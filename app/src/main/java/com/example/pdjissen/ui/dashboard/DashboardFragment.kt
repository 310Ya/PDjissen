package com.example.pdjissen.ui.dashboard // ← パッケージ名はお兄ちゃんの環境に合わせてね

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
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.pdjissen.R

// HomeFragment から SensorEventListener をこっちに持ってくるよ！
class DashboardFragment : Fragment(), SensorEventListener {

    // --- 歩数計のコードを HomeFragment からぜんぶ持ってきたよ！ ---
    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private lateinit var stepCountText: TextView // これが Dashboard の XML にある部品だね
    private val ACTIVITY_RECOGNITION_REQUEST_CODE = 100
    // --- ここまで ---

    private lateinit var textLocation: TextView // これは元々 Dashboard にあった部品かな？

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 1. 表示する XML を fragment_dashboard に変えるよ！
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // 2. fragment_dashboard.xml の中の部品を探すよ
        stepCountText = view.findViewById(R.id.step_count_text) // これでちゃんと見つかるはず！
        textLocation = view.findViewById(R.id.textLocation) // こっちも一応見つけておくね

        // 3. 準備ができたViewを返すよ
        return view
    }

    // このメソッドも HomeFragment から持ってきたよ
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 歩数計センサーの準備をするよ
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCounterSensor == null) {
            Toast.makeText(context, "歩数センサーが見つかりません", Toast.LENGTH_SHORT).show()
        }
    }

    // --- ここから下も、ぜんぶ HomeFragment から持ってきたコードだよ！ ---

    override fun onResume() {
        super.onResume()
        // 画面が表示されたら、権限をチェックしてセンサーを動かし始めるよ
        checkPermissionAndStart()
    }

    override fun onPause() {
        super.onPause()
        // 画面が隠れたら、センサーを止めて電池を節約するよ
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // センサーが新しい歩数を教えてくれたら、画面の文字を更新するよ
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val steps = event.values[0].toInt()
            stepCountText.text = "$steps 歩"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 何もしない
    }

    private fun checkPermissionAndStart() {
        // 「歩数計の権限、もう許可されてる？」ってチェックするよ
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
            == PackageManager.PERMISSION_GRANTED) {
            // 許可されてたら、歩数計をスタート！
            startStepCounter()
        } else {
            // 許可されてなかったら、「権限ください！」ってダイアログを出すよ
            requestPermissions(
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                ACTIVITY_RECOGNITION_REQUEST_CODE
            )
        }
    }

    private fun startStepCounter() {
        // 歩数計センサーに「これから歩数を数えてね」ってお願いするよ
        stepCounterSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // 「権限ください！」ダイアログの結果が返ってきたときの処理だよ
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACTIVITY_RECOGNITION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 許可してくれた！ありがとう！歩数計スタート！
                startStepCounter()
            } else {
                // 拒否されちゃった... (´・ω・`)
                // context じゃなくて requireContext() を使うように直したよ！
                Toast.makeText(requireContext(), "歩数計測には権限の許可が必要です", Toast.LENGTH_SHORT).show()
            }
        }
    }
}