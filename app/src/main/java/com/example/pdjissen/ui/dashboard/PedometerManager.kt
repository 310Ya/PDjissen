package com.example.pdjissen.ui.dashboard

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast

// 歩数計の「専門家」クラスだよ！
class PedometerManager(
    private val context: Context,
    private val onStepCounted: (Int) -> Unit // 歩数を数えたら、外（Fragment）に伝えるための「連絡係」
) : SensorEventListener {

    // --- 歩数計関連 ---
    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private var initialSteps: Int = -1 // 計測開始時の歩数
    private var isTracking = false // 計測中かどうか

    init {
        // 準備
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepCounterSensor == null) {
            Toast.makeText(context, "歩数センサーが見つかりません", Toast.LENGTH_SHORT).show()
        }
    }

    // --- 計測スタート ---
    fun start() {
        if (!isTracking) {
            isTracking = true
            initialSteps = -1 // リセット
            stepCounterSensor?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    // --- 計測ストップ ---
    fun stop() {
        if (isTracking) {
            isTracking = false
            sensorManager?.unregisterListener(this, stepCounterSensor)
        }
    }

    // --- SensorEventListener ---
    // 歩数が変わったら呼ばれる
    override fun onSensorChanged(event: SensorEvent?) {
        if (!isTracking) return // 計測中でなければ何もしない
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()
            if (initialSteps == -1) {
                initialSteps = totalSteps
            }
            val stepsSinceStart = totalSteps - initialSteps

            // 「連絡係」を通して、外（DashboardFragment）に「今、〜歩になったよ！」って伝える
            onStepCounted(stepsSinceStart)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* 使わない */ }
}