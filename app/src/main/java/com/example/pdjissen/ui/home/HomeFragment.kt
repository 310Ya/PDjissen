package com.example.pdjissen.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pdjissen.R

class HomeFragment : Fragment(), SensorEventListener {

    // 歩数計で使う部品
    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private lateinit var stepCountText: TextView
    private val ACTIVITY_RECOGNITION_REQUEST_CODE = 100

    private var initialSteps: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 1. まず、見た目のファイル（XML）を画面に表示するよ
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 2. 見た目のファイルから、動かしたい部品を探して変数に入れるよ
        stepCountText = view.findViewById(R.id.step_count_text)
        val startButton: Button = view.findViewById(R.id.btnStartMeasure)

        // 3. GPS計測開始ボタンに、「押されたらDashboard画面に移動してね」って命令するよ
        startButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_dashboard)
        }

        // 4. 準備ができたViewを返すよ
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 歩数計センサーの準備をするよ
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCounterSensor == null) {
            Toast.makeText(context, "歩数センサーが見つかりません", Toast.LENGTH_SHORT).show()
        }
    }

    // --- ここから下は、歩数計の機能のためのコードだよ ---

    override fun onResume() {
        super.onResume()
        checkPermissionAndStart()
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()

            // 最初の1回目のイベントだったら、その時の歩数を記録する
            if (initialSteps == -1) {
                initialSteps = totalSteps
            }

            // 現在の合計歩数から、最初の歩数を引いて、アプリ起動後の歩数を計算する
            val stepsSinceAppStart = totalSteps - initialSteps

            stepCountText.text = "$stepsSinceAppStart 歩"
        }
    }
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