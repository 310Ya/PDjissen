//地図関連
package com.example.pdjissen.ui.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

// 地図（GoogleMap）操作の「専門家」クラスだよ！
class MapManager(private val context: Context) {

    private var googleMap: GoogleMap? = null
    private var currentMarker: Marker? = null

    // --- 地図の準備ができたときに呼ばれる ---
    fun setupMap(map: GoogleMap) {
        googleMap = map
    }

    // --- 現在地表示を有効にする ---
    @SuppressLint("MissingPermission") // 権限チェックはFragment側でやるよ！
    fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
            googleMap?.uiSettings?.isMyLocationButtonEnabled = true
        }
    }

    // --- カメラを移動する（ズームあり） ---
    fun moveCameraTo(latLng: LatLng, zoomLevel: Float = 17f) {
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
    }

    // --- カメラを移動する（ズームなし・アニメーションあり） ---
    fun animateCameraTo(latLng: LatLng) {
        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    // --- 現在地マーカーを更新（または新規作成）する ---
    fun updateMarker(latLng: LatLng, title: String = "現在地") {
        if (currentMarker == null) {
            // マーカーがまだない -> 新しく作る
            currentMarker = googleMap?.addMarker(MarkerOptions().position(latLng).title(title))
        } else {
            // マーカーがある -> 位置だけ更新
            currentMarker?.position = latLng
        }
    }

    // --- Fragmentが破棄されるときに呼ぶ ---
    fun cleanup() {
        googleMap = null
        currentMarker = null
    }
}