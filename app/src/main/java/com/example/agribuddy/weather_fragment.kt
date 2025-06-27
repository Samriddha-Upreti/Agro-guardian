package com.example.agribuddy

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.agribuddy.databinding.FragmentWeatherFragmentBinding
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection

class WeatherFragment : Fragment() {
    private var _binding: FragmentWeatherFragmentBinding? = null
    private val binding get() = _binding!!

    private val apiKey: String = "9768f1e5b747b9aca3a865b2eedb2626"

    // Location related variables
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    companion object {
        private const val BASE_URL = "https://api.openweathermap.org/data/2.5/weather"
        private const val DEFAULT_LOCATION = "Kathmandu"
        private const val UNITS = "metric"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pressure.visibility = View.VISIBLE
        binding.visibility.visibility = View.VISIBLE
        binding.sunrise.visibility = View.VISIBLE
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.swipeRefreshLayout.setOnRefreshListener {
            checkLocationPermissionAndFetchWeather()
        }

        checkLocationPermissionAndFetchWeather()
    }

    private fun checkLocationPermissionAndFetchWeather() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showError("Location permission is needed to show weather for your current location")
                requestLocationPermission()
            }

            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    showError("Using default location - enable location permission for accurate weather")
                    fetchWeatherData(DEFAULT_LOCATION)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        createLocationRequest()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    fetchWeatherDataByCoordinates(location.latitude, location.longitude)
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                } ?: run {
                    showError("Could not get location, using default")
                    fetchWeatherData(DEFAULT_LOCATION)
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                if (!locationAvailability.isLocationAvailable) {
                    showError("Location not available, using default")
                    fetchWeatherData(DEFAULT_LOCATION)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun fetchWeatherData(location: String) {
        if (apiKey.isBlank()) {
            showError("API key not configured")
            return
        }

        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.errorMessage.visibility = View.GONE

                val weatherData = withContext(Dispatchers.IO) {
                    getWeatherFromApi(location)
                }

                updateUI(weatherData)
            } catch (e: Exception) {
                showError("Failed to fetch weather data: ${e.localizedMessage}")
                Toast.makeText(context, "Weather update failed", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun fetchWeatherDataByCoordinates(latitude: Double, longitude: Double) {
        if (apiKey.isBlank()) {
            showError("API key not configured")
            return
        }

        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.errorMessage.visibility = View.GONE

                val weatherData = withContext(Dispatchers.IO) {
                    getWeatherFromApiByCoordinates(latitude, longitude)
                }

                updateUI(weatherData)
            } catch (e: Exception) {
                showError("Failed to fetch weather data: ${e.localizedMessage}")
                Toast.makeText(context, "Weather update failed", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    @Throws(IOException::class)
    private suspend fun getWeatherFromApi(location: String): JSONObject {
        val url = "$BASE_URL?q=$location&units=$UNITS&appid=$apiKey"
        val connection = URL(url).openConnection() as HttpsURLConnection

        return try {
            connection.connectTimeout = 10000
            connection.readTimeout = 15000
            connection.connect()

            when (connection.responseCode) {
                HttpsURLConnection.HTTP_OK -> {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    JSONObject(response)
                }
                HttpsURLConnection.HTTP_UNAUTHORIZED -> {
                    throw IOException("Invalid API key - please check configuration")
                }
                else -> {
                    val error = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    val errorMsg = try {
                        JSONObject(error).optString("message", "Unknown error")
                    } catch (e: Exception) {
                        "HTTP ${connection.responseCode}"
                    }
                    throw IOException("API error: $errorMsg")
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    @Throws(IOException::class)
    private suspend fun getWeatherFromApiByCoordinates(latitude: Double, longitude: Double): JSONObject {
        val url = "$BASE_URL?lat=$latitude&lon=$longitude&units=$UNITS&appid=$apiKey"
        val connection = URL(url).openConnection() as HttpsURLConnection

        return try {
            connection.connectTimeout = 10000
            connection.readTimeout = 15000
            connection.connect()

            when (connection.responseCode) {
                HttpsURLConnection.HTTP_OK -> {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    JSONObject(response)
                }
                HttpsURLConnection.HTTP_UNAUTHORIZED -> {
                    throw IOException("Invalid API key - please check configuration")
                }
                else -> {
                    val error = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    val errorMsg = try {
                        JSONObject(error).optString("message", "Unknown error")
                    } catch (e: Exception) {
                        "HTTP ${connection.responseCode}"
                    }
                    throw IOException("API error: $errorMsg")
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun updateUI(weatherData: JSONObject) {
        Log.d("WeatherResponse", weatherData.toString(2))
        try {
            val main = weatherData.getJSONObject("main")
            val weather = weatherData.getJSONArray("weather").getJSONObject(0)
            val wind = weatherData.getJSONObject("wind")
            val sys = weatherData.getJSONObject("sys")

            val temp = main.getDouble("temp")
            val feelsLike = main.getDouble("feels_like")
            val humidity = main.getInt("humidity")
            val pressure = main.getDouble("pressure").toFloat()
            val visibility = weatherData.optInt("visibility", -1)
            val description = weather.getString("description")
            val iconCode = weather.getString("icon")
            val windSpeed = wind.getDouble("speed")

            val sunrise = sys.getLong("sunrise")
            val sunset = sys.getLong("sunset")
            val sunriseTime = convertUnixTimeToReadable(sunrise)
            val sunsetTime = convertUnixTimeToReadable(sunset)

            binding.location.text = weatherData.getString("name")
            binding.temperature.text = getString(R.string.temperature_format, temp)
            binding.weatherDescription.text = description.replaceFirstChar { it.uppercase() }
            binding.feelsLike.text = getString(R.string.feels_like_format, feelsLike)
            binding.humidity.text = getString(R.string.humidity_format, humidity)
            binding.windSpeed.text = getString(R.string.wind_speed_format, windSpeed)

            // New Fields
            binding.pressure.text = getString(R.string.pressure_format, pressure)
            binding.pressure.visibility = View.VISIBLE

            if (visibility != -1) {
                val visibilityInKm = visibility / 1000.0
                binding.visibility.text = getString(R.string.visibility_format, visibilityInKm)
                binding.visibility.visibility = View.VISIBLE
            } else {
                binding.visibility.visibility = View.GONE
            }

            binding.sunrise.text = getString(R.string.sunrise_format, sunriseTime)
            binding.sunrise.visibility = View.VISIBLE



            loadWeatherIcon(iconCode)
        } catch (e: Exception) {
            showError("Failed to parse weather data")
        }
        Log.d("BindingCheck", "Pressure text: ${binding.pressure.text}")
        Log.d("BindingCheck", "Pressure visible: ${binding.pressure.visibility == View.VISIBLE}")
    }


    private fun convertUnixTimeToReadable(unixTime: Long): String {
        val date = Date(unixTime * 1000) // Convert seconds to milliseconds
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(date)
    }

    private fun showError(message: String) {
        binding.errorMessage.text = message
        binding.errorMessage.visibility = View.VISIBLE
    }

    private fun loadWeatherIcon(iconCode: String) {
        val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
        // Implement using your preferred image loading library
        // Glide.with(requireContext()).load(iconUrl).into(binding.weatherIcon)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        _binding = null
    }
}