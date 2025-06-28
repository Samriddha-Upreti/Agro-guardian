package com.example.agribuddy

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

class SensorsActivity : AppCompatActivity() {

    private lateinit var fireSensorValue: TextView
    private lateinit var pirSensorValue: TextView
    private lateinit var temperatureValue: TextView
    private lateinit var humidityValue: TextView
    private lateinit var moistureValue: TextView
    private lateinit var progressBar: ProgressBar

    private val TAG = "SensorsActivity"
    private val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
    private val FETCH_INTERVAL: Long = 3000
    private val handler = Handler(Looper.getMainLooper())

    // Notification cooldown tracking
    private var lastTempNotificationTime = 0L
    private var lastHumidityNotificationTime = 0L
    private var lastFireNotificationTime = 0L
    private var lastPirNotificationTime = 0L
    private val cooldownTime = 5 * 60 * 1000 // 5 minutes

    // Thresholds
    private val TEMP_THRESHOLD = 35.0
    private val HUMIDITY_THRESHOLD = 80.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensors_data)

        // Initialize all views
        fireSensorValue = findViewById(R.id.fireSensorValue)
        pirSensorValue = findViewById(R.id.pirSensorValue)
        temperatureValue = findViewById(R.id.temperatureValue)
        humidityValue = findViewById(R.id.humidityValue)
        moistureValue = findViewById(R.id.moistureValue)
        progressBar = findViewById(R.id.progressBar)

        if (!checkPlayServices()) {
            Toast.makeText(this, getString(R.string.google_play_required), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        fetchSensorData()
    }

    override fun onResume() {
        super.onResume()
        startPeriodicDataFetch()
    }

    override fun onPause() {
        super.onPause()
        stopPeriodicDataFetch()
    }

    private fun startPeriodicDataFetch() {
        handler.post(fetchDataRunnable)
    }

    private fun stopPeriodicDataFetch() {
        handler.removeCallbacks(fetchDataRunnable)
    }

    private val fetchDataRunnable = object : Runnable {
        override fun run() {
            fetchSensorData()
            handler.postDelayed(this, FETCH_INTERVAL)
        }
    }

    private fun fetchSensorData() {
        if (!isNetworkAvailable()) {
            showNetworkError()
            return
        }

        progressBar.visibility = View.VISIBLE

        try {
            val dbUrl = getString(R.string.firebase_db_url)
            val ref = FirebaseDatabase.getInstance(dbUrl).getReference("sensors")

            Log.d(TAG, "Fetching sensor data from: $dbUrl")

            ref.get().addOnSuccessListener { snapshot ->
                progressBar.visibility = View.GONE
                handlePirSensor(snapshot)
                handleDhtSensor(snapshot)
                handleFireSensor(snapshot)
                handleMoistureSensor(snapshot)

            }.addOnFailureListener { error ->
                progressBar.visibility = View.GONE
                Log.e(TAG, "Data fetch error", error)
                showErrorUI()
                Toast.makeText(this, getString(R.string.error_loading_data), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            progressBar.visibility = View.GONE
            Log.e(TAG, "Unexpected error", e)
            showErrorUI()
        }
    }

    private fun handleDhtSensor(snapshot: DataSnapshot) {
        val dht = snapshot.child("dht")
        val currentTime = System.currentTimeMillis()

        val temp = dht.child("temperature").getValue(Double::class.java)
        val humidity = dht.child("humidity").getValue(Double::class.java)

        temperatureValue.text = if (temp != null)
            getString(R.string.temperature_format, temp)
        else
            getString(R.string.temperature_na)

        humidityValue.text = if (humidity != null)
            getString(R.string.humidity_format, humidity.toInt())
        else
            getString(R.string.humidity_na)

        if (temp != null && temp > TEMP_THRESHOLD && currentTime - lastTempNotificationTime > cooldownTime) {
            lastTempNotificationTime = currentTime
            NotificationManagerSingleton.addNotification(
                this,
                getString(R.string.alert_temp_title),
                getString(R.string.alert_temp_msg, temp, TEMP_THRESHOLD)
            )
        }

        if (humidity != null && humidity > HUMIDITY_THRESHOLD && currentTime - lastHumidityNotificationTime > cooldownTime) {
            lastHumidityNotificationTime = currentTime
            NotificationManagerSingleton.addNotification(
                this,
                getString(R.string.alert_humidity_title),
                getString(R.string.alert_humidity_msg, humidity, HUMIDITY_THRESHOLD)
            )
        }
    }

    private fun handlePirSensor(snapshot: DataSnapshot) {
        val pir = snapshot.child("pir").getValue(Long::class.java) == 1L

        pirSensorValue.text = if (pir) getString(R.string.motion_detected) else getString(R.string.no_motion)

        if (pir && System.currentTimeMillis() - lastPirNotificationTime > cooldownTime) {
            lastPirNotificationTime = System.currentTimeMillis()
            NotificationManagerSingleton.addNotification(
                this,
                getString(R.string.motion_detected),
                getString(R.string.motion_detected_msg)
            )
        }
    }

    private fun handleFireSensor(snapshot: DataSnapshot) {
        val fire = snapshot.child("fire").getValue(Long::class.java) == 1L

        fireSensorValue.text = if (fire) getString(R.string.fire_detected) else getString(R.string.no_fire)

        if (fire && System.currentTimeMillis() - lastFireNotificationTime > cooldownTime) {
            lastFireNotificationTime = System.currentTimeMillis()
            NotificationManagerSingleton.addNotification(
                this,
                getString(R.string.fire_detected),
                getString(R.string.fire_detected_msg)
            )
        }
    }

    private fun handleMoistureSensor(snapshot: DataSnapshot) {
        val moisture = snapshot.child("moisture").getValue(Int::class.java)
        moistureValue.text = if (moisture != null)
            getString(R.string.moisture_level, moisture)
        else
            getString(R.string.moisture_level, 0)
    }

    private fun showErrorUI() {
        runOnUiThread {
            val error = getString(R.string.error_text)
            pirSensorValue.text = error
            temperatureValue.text = error
            humidityValue.text = error
            moistureValue.text = error
            fireSensorValue.text = error
        }
    }

    private fun showNetworkError() {
        runOnUiThread {
            Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_SHORT).show()
            val offline = getString(R.string.offline_text)
            pirSensorValue.text = offline
            temperatureValue.text = offline
            humidityValue.text = offline
            moistureValue.text = offline
            fireSensorValue.text = offline
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = cm.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun checkPlayServices(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)?.show()
            } else {
                Toast.makeText(this, getString(R.string.unsupported_device), Toast.LENGTH_LONG).show()
            }
            return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
