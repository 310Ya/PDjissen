//GPS関係
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
class DashboardFragment : Fragment(), OnMapReadyCallback {

    // --- 見た目の部品 ---
    private lateinit var textDistance: TextView
    private lateinit var stepCountText: TextView // 歩数表示用 (IDをXMLに合わせたよ)
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var mapView: MapView
    //private var googleMap: GoogleMap? = null
    //private var currentMarker: Marker? = null

    // --- 歩数計関連 ---
    private var pedometerManager: PedometerManager? = null // 歩数計の専門家を呼ぶよ！

    // --- GPS関連 (MainActivityから持ってきた！) ---
    // ★ ここはごっそり書き換えるよ！ ★
    private var locationTracker: LocationTracker? = null // GPSの専門家を呼ぶよ！
    // ★ 地図の専門家を呼ぶよ！ ★
    private var mapManager: MapManager? = null
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
                enableMyLocationOnMap()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // 大まかな位置情報の権限だけ許可された場合
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
            locationTracker?.start()// GPSもスタート
            pedometerManager?.start() // 歩数計を開始
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
        // ★ MapManagerを準備する ★
        mapManager = MapManager(requireContext())

        // GPSクライアントの準備 (MainActivityから持ってきた！)
        locationTracker = LocationTracker(requireContext()) { location ->
            // ↑ 新しい位置情報が来たら、ここに連絡が来る！
            updateLocationUI(location) // 前からある地図更新の処理を呼ぶ
        }


        // 歩数計センサーの準備
        pedometerManager = PedometerManager(requireContext()) { stepsSinceStart ->
            // ↑ 歩数を数えたら、ここに連絡が来る！
            stepCountText.text = "歩数: $stepsSinceStart 歩"
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
        // ★ 専門家（MapManager）に地図を渡す ★
        mapManager?.setupMap(map)

        // まず、位置情報の権限がすでにあるか確認する
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 権限がある！ -> 専門家に「現在地表示して！」ってお願い
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
        // ★ 専門家にお願いする ★
        mapManager?.enableMyLocation()

        // 最後に取得した現在地（LastLocation）にカメラを移動する
        getLastLocationAndMoveCamera()
    }

    // --- SensorEventListener ---

    // --- 計測開始・終了ロジック ---
    private fun startTracking() {
        if (!isTracking) {
            isTracking = true
            totalDistance = 0f // 距離リセット
            lastLocation = null // 前回の位置リセット
            textDistance.text = "移動距離: 0.0 m"
            stepCountText.text = "歩数: 0 歩" // 表示もリセット

            checkPermissionsAndStartSensors() // 権限を確認してセンサー類を開始

            Toast.makeText(context, "計測を開始しました", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopTracking() {
        if (isTracking) {
            isTracking = false
            // ★ 位置情報更新を停止（専門家にお願いする） ★
            locationTracker?.stop() // 修正
            pedometerManager?.stop()
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
            // ★ 位置情報も開始（専門家にお願いする） ★
            locationTracker?.start() // 修正 (startLocationUpdatesWithCheck() ではなく)
            pedometerManager?.start()
        } else {
            // 歩数計の権限がない -> リクエスト
            activityRecognitionPermissionRequest.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }


    // --- 位置情報関連 (MainActivityから持ってきた！) ---
    //@SuppressLint("MissingPermission") // 権限チェックは呼び出し元で行う


    @SuppressLint("MissingPermission")
    private fun getLastLocationAndMoveCamera() {
        locationTracker?.getLastKnownLocation { location: Location? ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                // ★ 専門家にお願いする ★
                mapManager?.moveCameraTo(currentLatLng)
                mapManager?.updateMarker(currentLatLng)
            }
        }
    }


    // 新しい位置情報でUIを更新 (MainActivityから持ってきた！)
    private fun updateLocationUI(location: Location) {
        val currentLatLng = LatLng(location.latitude, location.longitude)

        // ★ 専門家にお願いする ★
        mapManager?.updateMarker(currentLatLng)
        // 最初の位置にカメラ移動（計測開始時のみ）
        if (!isTracking) {
            mapManager?.moveCameraTo(currentLatLng)
        } else {
            // 計測中はカメラをスムーズに移動
            mapManager?.animateCameraTo(currentLatLng)
        }

        // 距離計測 (計測中の場合のみ)
        if (isTracking && lastLocation != null) {
            totalDistance += lastLocation!!.distanceTo(location)
            textDistance.text = String.format("移動距離: %.2f m", totalDistance)
        }
        lastLocation = location // 今回の位置を次回のために覚えておく
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
        locationTracker?.stop()
        // ★ 歩数計を停止（専門家にお願いする） ★
        pedometerManager?.stop() // 修正
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
        mapManager?.cleanup()
        mapManager = null
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