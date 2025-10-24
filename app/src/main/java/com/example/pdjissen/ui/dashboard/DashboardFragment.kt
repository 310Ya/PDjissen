package com.example.pdjissen.ui.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.pdjissen.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.concurrent.TimeUnit

// SensorEventListener と OnMapReadyCallback の両方を実装するよ！
class DashboardFragment : Fragment(), SensorEventListener, OnMapReadyCallback {

    // --- 見た目の部品 ---
    private lateinit var textDistance: TextView
    private lateinit var stepCountText: TextView // 歩数表示用 (IDをXMLに合わせたよ)
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private var currentMarker: Marker? = null

    // --- 歩数計関連 ---
    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private var initialSteps: Int = -1 // アプリ起動後の歩数計算用

    // --- GPS関連 (MainActivityから持ってきた！) ---
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var lastLocation: Location? = null
    private var totalDistance = 0f
    private var isTracking = false // 計測中かどうか

    // --- 権限関連 ---
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // 位置情報の権限が許可された！
                // startLocationUpdatesWithCheck() // すぐに計測は開始しない
                enableMyLocationOnMap() // マップの現在地表示を有効にする
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // 大まかな位置情報の権限だけ許可された場合
                // startLocationUpdatesWithCheck()
                enableMyLocationOnMap()
            } else -> {
            // 位置情報の権限が拒否された...
            Toast.makeText(context, "位置情報の権限がないと地図機能は使えません", Toast.LENGTH_SHORT).show()
        }
        }
    }
    private val activityRecognitionPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 歩数計の権限が許可された！
            // 権限チェックフローの最後なので、両方スタート！
            startLocationUpdatesWithCheck() // GPSもスタート
            startStepCounter() // 歩数計を開始
        } else {
            // 歩数計の権限が拒否された...
            Toast.makeText(context, "身体活動の権限がないと歩数計機能は使えません", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // 部品を見つけてくる
        textDistance = view.findViewById(R.id.textDistance)
        stepCountText = view.findViewById(R.id.step_count_text) // ID変更！
        btnStart = view.findViewById(R.id.btnStartMeasure)
        btnStop = view.findViewById(R.id.btnStopMeasure)
        mapView = view.findViewById(R.id.mapView)

        // MapViewのライフサイクルをFragmentに連動させるための初期化
        mapView.onCreate(savedInstanceState)
        // 地図の準備ができたら教えてもらうように設定
        mapView.getMapAsync(this)

        // GPSクライアントの準備 (MainActivityから持ってきた！)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // 位置情報リクエストの設定
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, // 精度を「バランス型」に変更
            TimeUnit.SECONDS.toMillis(5) // 5秒ごとに更新
        )
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5)) // 最短更新間隔も5秒に設定
            .build()

        // 位置情報を受け取ったときの処理 (MainActivityから持ってきた！)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateLocationUI(location)
                }
            }
        }

        // 歩数計センサーの準備
        sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepCounterSensor == null) {
            Toast.makeText(context, "歩数センサーが見つかりません", Toast.LENGTH_SHORT).show()
        }

        // 計測開始ボタンの処理
        btnStart.setOnClickListener {
            startTracking()
        }

        // 計測終了ボタンの処理
        btnStop.setOnClickListener {
            stopTracking()
        }

        return view
    }

    // --- OnMapReadyCallback ---
    // 地図の準備ができたら呼ばれる (MainActivityから持ってきた！)
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // まず、位置情報の権限がすでにあるか確認する
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 権限がある！ -> 新しい関数を呼ぶ
            enableMyLocationOnMap()

        } else {
            // 権限がまだない... -> 権限をリクエストする！
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }
    @SuppressLint("MissingPermission") // 呼び出し元で権限チェックしてる前提
    private fun enableMyLocationOnMap() {
        // googleMapがnullじゃない（onMapReadyが呼ばれた後）か確認
        if (googleMap != null) {
            // 1. マップに「現在地ボタン（青い丸）」を有効にする
            googleMap?.isMyLocationEnabled = true
            googleMap?.uiSettings?.isMyLocationButtonEnabled = true // 右上の「現在地に戻る」ボタン

            // 2. 最後に取得した現在地（LastLocation）にカメラを移動する
            getLastLocationAndMoveCamera()
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
            stepCountText.text = "歩数: $stepsSinceStart 歩"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* 使わない */ }

    // --- 計測開始・終了ロジック ---
    private fun startTracking() {
        if (!isTracking) {
            isTracking = true
            totalDistance = 0f // 距離リセット
            lastLocation = null // 前回の位置リセット
            initialSteps = -1 // 歩数計の初期値リセット
            textDistance.text = "移動距離: 0.0 m"
            stepCountText.text = "歩数: 0 歩" // 表示もリセット

            checkPermissionsAndStartSensors() // 権限を確認してセンサー類を開始

            Toast.makeText(context, "計測を開始しました", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopTracking() {
        if (isTracking) {
            isTracking = false
            stopLocationUpdates() // 位置情報更新を停止
            stopStepCounter() // 歩数計を停止
            Toast.makeText(context, "計測を終了しました", Toast.LENGTH_SHORT).show()
        }
    }

    // --- 権限チェックとセンサー開始 ---
    private fun checkPermissionsAndStartSensors() {
        // 1. 位置情報の権限チェック
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 位置情報OK -> 2. 歩数計の権限チェック
            checkActivityRecognitionPermissionAndStartStepCounter()
        } else {
            // 位置情報の権限がない -> リクエスト
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
            // 注意: 位置情報の許可がないと歩数計も開始しないように一旦しておく
            //       (両方許可されてから開始する方がシンプル)
        }
    }

    private fun checkActivityRecognitionPermissionAndStartStepCounter() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            // 歩数計OK -> 両方の権限がOKなので、両方スタート！
            startLocationUpdatesWithCheck()
            startStepCounter()
        } else {
            // 歩数計の権限がない -> リクエスト
            activityRecognitionPermissionRequest.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }


    // --- 位置情報関連 (MainActivityから持ってきた！) ---
    @SuppressLint("MissingPermission") // 権限チェックは呼び出し元で行う
    private fun startLocationUpdatesWithCheck() {
        // すでに権限があるはずなので、最後の現在地を取得してカメラ移動
        getLastLocationAndMoveCamera()
        // 位置情報の更新を開始
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocationAndMoveCamera() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                // マーカーも更新
                currentMarker?.remove() // 古いマーカーを消す
                currentMarker = googleMap?.addMarker(MarkerOptions().position(currentLatLng).title("現在地"))
            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 新しい位置情報でUIを更新 (MainActivityから持ってきた！)
    private fun updateLocationUI(location: Location) {
        val currentLatLng = LatLng(location.latitude, location.longitude)

        // マーカーを更新
        if (currentMarker == null && googleMap != null) {
            currentMarker = googleMap?.addMarker(MarkerOptions().position(currentLatLng).title("現在地"))
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f)) // 最初の位置にカメラ移動
        } else {
            currentMarker?.position = currentLatLng
            googleMap?.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng)) // カメラをスムーズに移動
        }


        // 距離計測 (計測中の場合のみ)
        if (isTracking && lastLocation != null) {
            totalDistance += lastLocation!!.distanceTo(location)
            textDistance.text = String.format("移動距離: %.2f m", totalDistance)
        }
        lastLocation = location // 今回の位置を次回のために覚えておく
    }

    // --- 歩数計関連 ---
    private fun startStepCounter() {
        stepCounterSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun stopStepCounter() {
        sensorManager?.unregisterListener(this, stepCounterSensor)
    }

    // --- MapViewのライフサイクル管理 ---
    // これらをちゃんと呼んであげないと地図がうまく動かないよ！
    override fun onResume() {
        super.onResume()
        mapView.onResume()
        // もし計測中なら、センサーのリスナーを再登録
        if (isTracking) {
            checkPermissionsAndStartSensors() // 権限を再確認して開始
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        // バッテリー節約のため、センサーと位置情報の取得を止める
        stopLocationUpdates()
        stopStepCounter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
        googleMap = null // 地図オブジェクトを解放
        currentMarker = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}