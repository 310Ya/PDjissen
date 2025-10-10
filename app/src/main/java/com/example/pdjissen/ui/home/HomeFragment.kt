package com.example.pdjissen.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.pdjissen.R
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat

class HomeFragment : Fragment(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private lateinit var stepCountText: TextView
    private val ACTIVITY_RECOGNITION_REQUEST_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        // XMLで定義したIDを使ってTextViewを見つけるよ
        stepCountText = view.findViewById(R.id.step_count_text)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCounterSensor == null) {
            Toast.makeText(context, "歩数センサーが見つかりません", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissionAndStart()
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

        // ボタンを取得
        val startButton: Button = view.findViewById(R.id.btnStartMeasure)
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val steps = event.values[0].toInt()
            stepCountText.text = "$steps 歩"
        }
    }

        // ボタンを押したらDashboardへ遷移
        startButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_dashboard)
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 何もしない
    }

    private fun checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
            == PackageManager.PERMISSION_GRANTED) {
            startStepCounter()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                ACTIVITY_RECOGNITION_REQUEST_CODE
            )
        }
    }

        return view
    private fun startStepCounter() {
        stepCounterSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACTIVITY_RECOGNITION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStepCounter()
            } else {
                Toast.makeText(context, "歩数計測には権限の許可が必要です", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
