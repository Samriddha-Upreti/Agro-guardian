package com.example.agribuddy

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class SensorsActivity : AppCompatActivity() {

    private lateinit var pirSensorValue: TextView
    private lateinit var temperatureValue: TextView
    private lateinit var humidityValue: TextView
    private lateinit var moistureValue: TextView
    private lateinit var progressBar: ProgressBar

    private val TAG = "SensorsActivity"

    // Interval time in milliseconds (e.g., 10 seconds)
    private val FETCH_INTERVAL: Long = 3000  // 10 seconds
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensors_data) // Make sure this layout is named activity_sensors.xml

        // Initialize views
        pirSensorValue = findViewById(R.id.pirSensorValue)
        temperatureValue = findViewById(R.id.temperatureValue)
        humidityValue = findViewById(R.id.humidityValue)
        moistureValue = findViewById(R.id.moistureValue)
        progressBar = findViewById(R.id.progressBar)

        // Fetch data initially and start periodic fetch
        fetchSensorData()
        startPeriodicDataFetch()
    }

    private fun startPeriodicDataFetch() {
        // Runnable to periodically fetch data
        val fetchDataRunnable = object : Runnable {
            override fun run() {
                fetchSensorData()
                handler.postDelayed(this, FETCH_INTERVAL)  // Schedule next data fetch
            }
        }

        handler.post(fetchDataRunnable)  // Start the periodic data fetch
    }

    private fun fetchSensorData() {
        progressBar.visibility = View.VISIBLE

        val dbUrl = "https://agribuddy-24059-default-rtdb.asia-southeast1.firebasedatabase.app/"
        val ref = FirebaseDatabase.getInstance(dbUrl).getReference("sensors")

        ref.get().addOnSuccessListener { snapshot ->
            progressBar.visibility = View.GONE
            Log.d(TAG, "Data fetched: ${snapshot.value}")

            // PIR
            val pirRaw = snapshot.child("pir").getValue(Long::class.java)
            val pir = pirRaw == 1L  // Treat 1 as true, 0 as false

            pirSensorValue.text = when (pir) {
                true -> "Motion Detected!"
                false -> "No Motion"
            }

            // Temperature & Humidity (DHT11)
            val dht = snapshot.child("dht11")
            val temp = dht.child("temperature").getValue(Double::class.java)
                ?: dht.child("temperature").getValue(String::class.java)?.toDoubleOrNull()
            val humidity = dht.child("humidity").getValue(Double::class.java)
                ?: dht.child("humidity").getValue(String::class.java)?.toDoubleOrNull()

            temperatureValue.text = "Temperature: ${formatValue(temp, "°C")}"
            humidityValue.text = "Humidity: ${formatValue(humidity, "%")}"

            // Moisture
            val moisture = snapshot.child("moisture").getValue(Int::class.java)
            moistureValue.text = "Moisture Level: ${moisture ?: "N/A"}%"

        }.addOnFailureListener { error ->
            progressBar.visibility = View.GONE
            Log.e(TAG, "Error fetching data", error)
            Toast.makeText(this, "❌ Error loading data", Toast.LENGTH_SHORT).show()
            showErrorUI()
        }
    }

    private fun formatValue(value: Double?, unit: String): String {
        return value?.let {
            // Escape % symbols to avoid format string crashes
            val safeUnit = unit.replace("%", "%%")
            "%.1f$safeUnit".format(it)
        } ?: "N/A"
    }

    private fun showErrorUI() {
        pirSensorValue.text = "Error"
        temperatureValue.text = "Error"
        humidityValue.text = "Error"
        moistureValue.text = "Error"
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)  // Stop periodic fetching when the activity is destroyed
    }
}
