package com.example.climo.alerts.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.climo.alerts.receiver.AlertReceiver
import com.example.climo.data.local.ClimoDatabase
import com.example.climo.data.remote.RetrofitClient
import com.example.climo.data.repository.WeatherAlertRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class AlertWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val alertId = inputData.getInt("alertId" , 0)
        val cityName = inputData.getString("cityName") ?: "Unknown"
        val fromDateTime = inputData.getString("fromDateTime") ?: ""
        val toDateTime = inputData.getString("toDateTime") ?: ""
        val alertType = inputData.getString("alertType") ?: "NOTIFICATION"
        val latitude = inputData.getDouble("latitude", 0.0)
        val longitude = inputData.getDouble("longitude", 0.0)

//        Log.d("AlertWorker", "Executing work for alertId: $alertId, city: $cityName, type: $alertType")

        Log.d("AlertWorker", "Starting work for alertId: $alertId, city: $cityName, type: $alertType")

        if (alertId == 0) {
            Log.e("AlertWorker", "Invalid alertId: $alertId")
            return Result.failure()
        }
        // Fetch weather description
        val repository = WeatherAlertRepository(ClimoDatabase.getDatabase(applicationContext).weatherAlertDao())
        var weatherDescription = runBlocking {
            repository.getWeatherDescription(latitude, longitude)
        }
        try {
            val response = runBlocking {
                RetrofitClient.api.getCurrentWeather(
                    latitude,
                    longitude,
                    "ecfe2681690524ece36e0e4818523e5f"
                ).execute()
            }
            if (response.isSuccessful) {
                response.body()?.weather?.firstOrNull()?.description?.let {
                    weatherDescription = it.capitalize()
                }
            } else {
                Log.e("AlertWorker", "Weather API error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("AlertWorker", "Error fetching weather: ${e.message}")
        }


         val intent = Intent(applicationContext , AlertReceiver::class.java).apply{
             action = "com.example.climo.ALERT"
             putExtra("alertId", alertId)
             putExtra("cityName", cityName)
             putExtra("fromDateTime", fromDateTime)
             putExtra("toDateTime", toDateTime)
             putExtra("alertType", alertType)
             putExtra("weatherDescription", weatherDescription)
         }
        applicationContext.sendBroadcast(intent)
        Log.d("AlertWorker", "Broadcast sent for alertId: $alertId")

        return Result.success()
    }

}