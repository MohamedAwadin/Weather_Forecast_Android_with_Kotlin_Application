package com.example.climo.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.climo.data.local.WeatherAlertDao
import com.example.climo.data.model.WeatherAlert
import com.example.climo.data.remote.RetrofitClient

class WeatherAlertRepository(private val weatherAlertDao: WeatherAlertDao) {

    fun getActiveAlertsLiveData(currentDateTime: String): LiveData<List<WeatherAlert>> {
        return weatherAlertDao.getActiveAlertsLiveData(currentDateTime)
    }

    suspend fun insertAlert(alert: WeatherAlert): Long {
        return try {
            val id = weatherAlertDao.insert(alert)
            Log.d("WeatherAlertRepository", "Inserted alert: $alert, ID: $id")
            id
        } catch (e: Exception) {
            Log.e("WeatherAlertRepository", "Error inserting alert: ${e.message}")
            -1
        }
    }

    suspend fun deleteAlert(alert: WeatherAlert) {
        try {
            weatherAlertDao.delete(alert)
            Log.d("WeatherAlertRepository", "Deleted alert: $alert")
        } catch (e: Exception) {
            Log.e("WeatherAlertRepository", "Error deleting alert: ${e.message}")
        }
    }

    suspend fun deleteExpiredAlerts(currentDateTime: String) {
        try {
            weatherAlertDao.deleteExpiredAlerts(currentDateTime)
            Log.d("WeatherAlertRepository", "Deleted expired alerts before: $currentDateTime")
        } catch (e: Exception) {
            Log.e("WeatherAlertRepository", "Error deleting expired alerts: ${e.message}")
        }
    }

    suspend fun getWeatherDescription(latitude: Double, longitude: Double): String {
        return try {
            val response = RetrofitClient.api.getCurrentWeather(
                latitude,
                longitude,
                "ecfe2681690524ece36e0e4818523e5f"
            ).execute()
            if (response.isSuccessful) {
                response.body()?.weather?.firstOrNull()?.description?.capitalize() ?: "Weather data unavailable"
            } else {
                Log.e("WeatherAlertRepository", "Weather API error: ${response.code()}")
                "Weather data unavailable"
            }
        } catch (e: Exception) {
            Log.e("WeatherAlertRepository", "Error fetching weather: ${e.message}")
            "Weather data unavailable"
        }
    }
}