package com.example.pdjissen

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.pdjissen.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var lastLocation: Location? = null
    private var totalDistance = 0f
    private var isTracking = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 地図フラグメントの取得
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // ボタンイベント
        binding.btnStart.setOnClickListener {
            totalDistance = 0f
            lastLocation = null
            // UIをリセット
            binding.textDistance.text = String.format("移動距離: %.2f m", totalDistance)
            isTracking = true
            startLocationUpdates()
        }

        binding.btnStop.setOnClickListener {
            isTracking = false
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableMyLocation()
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        // LocationRequestの優先度をPRIORITY_HIGH_ACCURACYに設定し、更新間隔を2秒に設定
        val locationRequest = LocationRequest.Builder(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // onLocationResultは複数の位置情報を返すことがあるため、最新の位置情報を使う
                locationResult.lastLocation?.let { location ->
                    updateLocation(location)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    // --- ここからが修正された関数です ---
    private fun updateLocation(location: Location) {
        // ========== 修正点 1: 精度の低い位置情報を無視 ==========
        // 精度が20メートルより悪い場合は、処理を行わない
        if (location.accuracy > 20.0f) {
            return
        }

        val currentLatLng = LatLng(location.latitude, location.longitude)

        // カメラを移動
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))

        // マーカーを更新
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(currentLatLng).title("現在地"))

        // 距離計測
        if (isTracking) {
            // 最初の位置情報の場合は、lastLocationに設定して処理を終える
            if (lastLocation == null) {
                lastLocation = location
                return
            }

            // 2点間の距離を計算
            val distance = lastLocation!!.distanceTo(location)

            // ========== 修正点 2: 静止時のブレ（小さな移動）を無視 ==========
            // 例えば2メートル以上の移動があった場合のみ距離を加算する
            if (distance > 1.0f) {
                totalDistance += distance
                binding.textDistance.text = String.format("移動距離: %.2f m", totalDistance)
            }

            // 最後の位置情報を更新
            lastLocation = location
        }
    }
    // --- ここまでが修正された関数です ---


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults) // superの呼び出しを追加
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 権限が許可された場合
                enableMyLocation()
            }
        }
    }
}
