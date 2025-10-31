package com.example.pdjissen.ui.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import java.util.concurrent.TimeUnit

// GPS（位置情報）の「専門家」クラスだよ！
class LocationTracker(
    private val context: Context,
    private val onLocationUpdated: (Location) -> Unit // 位置情報が取れたら、外（Fragment）に伝える「連絡係」
) {

    // --- GPS関連 ---
    private var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback
    private var locationRequest: LocationRequest

    init {
        // GPSクライアントの準備
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // 位置情報リクエストの設定 (1秒ごとに最高精度)
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            TimeUnit.SECONDS.toMillis(1)
        )
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(1))
            .build()

        // 位置情報を受け取ったときの処理
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // 「連絡係」を通して、外（DashboardFragment）に「新しい位置情報が来たよ！」って伝える
                    onLocationUpdated(location)
                }
            }
        }
    }

    // --- 計測スタート ---
    @SuppressLint("MissingPermission") // 権限チェックはFragment側でやるよ！
    fun start() {
        Log.d("LocationTracker", "位置情報の更新を開始します")
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: Exception) {
            Log.e("LocationTracker", "requestLocationUpdatesでエラー", e)
        }
    }

    // --- 計測ストップ ---
    fun stop() {
        Log.d("LocationTracker", "位置情報の更新を停止します")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // --- 最後の現在地を取得 ---
    @SuppressLint("MissingPermission") // 権限チェックはFragment側でやるよ！
    fun getLastKnownLocation(onSuccess: (Location?) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                onSuccess(location) // 見つかった場所を「連絡係」で伝える
            }
            .addOnFailureListener {
                onSuccess(null) // 見つからなかったら null を伝える
            }
    }
}