package com.example.climo.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.preference.PreferenceManager
import android.util.Log

import com.example.climo.data.local.ClimoDatabase
import com.example.climo.data.model.WeatherData
import com.example.climo.data.remote.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(private val database: ClimoDatabase, private val context: Context) {

    suspend fun getWeatherData(lat: Double, lon: Double): WeatherData? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val units = sharedPreferences.getString("temp_unit", "metric") ?: "metric"
        val lang = sharedPreferences.getString("language", "en") ?: "en"
        val locationId = "${lat}_${lon}"
        return withContext(Dispatchers.IO) {
            try {
                if (isNetworkAvailable()) {
                    Log.d("WeatherRepository", "Fetching weather data for lat: $lat, lon: $lon, units: $units, lang: $lang")
                    val currentWeather = RetrofitClient.api.getCurrentWeather(
                        lat, lon, "ecfe2681690524ece36e0e4818523e5f", units , lang
                    )
                    val hourlyForecast = RetrofitClient.api.getHourlyForecast(
                        lat, lon, 24, "ecfe2681690524ece36e0e4818523e5f", units , lang
                    )
                    val dailyForecast = RetrofitClient.api.getDailyForecast(
                        lat, lon, 7, "ecfe2681690524ece36e0e4818523e5f", units , lang
                    )
                    Log.d("WeatherRepository", "Current weather: $currentWeather")
                    Log.d("WeatherRepository", "Hourly forecast: $hourlyForecast")
                    Log.d("WeatherRepository", "Daily forecast: $dailyForecast")

                    val weatherData = WeatherData(
                        locationId = locationId,
                        latitude = lat,
                        longitude = lon,
                        currentTemp = currentWeather.main.temp,
                        weatherDescription = currentWeather.weather.first().description,
                        weatherIcon = currentWeather.weather.first().icon,
                        dateTime = currentWeather.dt,
                        pressure = currentWeather.main.pressure,
                        humidity = currentWeather.main.humidity,
                        windSpeed = currentWeather.wind.speed,
                        cloudPercentage = currentWeather.clouds.all,
                        visibility = currentWeather.visibility,
                        hourlyForecast = Gson().toJson(hourlyForecast.hourly),
                        dailyForecast = Gson().toJson(dailyForecast.daily)
                    )

                    Log.d("WeatherRepository", "Saving weather data to database: $weatherData")
                    database.weatherDataDao().delete(locationId)
                    database.weatherDataDao().insert(weatherData)
                    weatherData
                } else {
                    Log.d("WeatherRepository", "No network, loading cached data for $locationId")
                    database.weatherDataDao().getWeatherData(locationId)
                }
            } catch (e: Exception) {
                Log.e("WeatherRepository", "Error fetching weather data: ${e.message}", e)
                database.weatherDataDao().getWeatherData(locationId)
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}