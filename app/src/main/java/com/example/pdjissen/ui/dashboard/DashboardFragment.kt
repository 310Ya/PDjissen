package com.example.pdjissen.ui.dashboard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.pdjissen.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class DashboardFragment : Fragment(), SensorEventListener, OnMapReadyCallback {

    private lateinit var textDistance: TextView
    private lateinit var textSteps: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private var currentMarker: Marker? = null

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var stepCount = 0

    private var locationManager: LocationManager? = null
    private var startLocation: Location? = null
    private var distance = 0f
    private var measuring = false

    private val LOCATION_PERMISSION_CODE = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        textDistance = view.findViewById(R.id.textDistance)
        textSteps = view.findViewById(R.id.textSteps)
        btnStart = view.findViewById(R.id.btnStartMeasure)
        btnStop = view.findViewById(R.id.btnStopMeasure)
        mapView = view.findViewById(R.id.mapView)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        btnStart.setOnClickListener { startMeasurement() }
        btnStop.setOnClickListener { stopMeasurement() }

        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
        }
    }

    private fun startMeasurement() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
            return
        }

        measuring = true
        stepCount = 0
        distance = 0f
        startLocation = null

        stepSensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }

        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L,
            1f,
            locationListener
        )

        Toast.makeText(context, "計測開始！", Toast.LENGTH_SHORT).show()
    }

    private fun stopMeasurement() {
        measuring = false
        sensorManager?.unregisterListener(this)
        locationManager?.removeUpdates(locationListener)
        Toast.makeText(context, "計測終了！移動距離: %.1f m".format(distance), Toast.LENGTH_LONG).show()
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (!measuring) return
            startLocation?.let {
                distance += it.distanceTo(location)
            }
            startLocation = location
            textDistance.text = "移動距離: %.1f m".format(distance)

            val latLng = LatLng(location.latitude, location.longitude)
            if (currentMarker == null) {
                currentMarker = googleMap?.addMarker(MarkerOptions().position(latLng).title("現在地"))
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
            } else {
                currentMarker?.position = latLng
                googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            }
        }
    }

    override fun onSensorChanged(event: android.hardware.SensorEvent?) {
        if (!measuring) return
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            stepCount = event.values[0].toInt()
            textSteps.text = "歩数: $stepCount 歩"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}

