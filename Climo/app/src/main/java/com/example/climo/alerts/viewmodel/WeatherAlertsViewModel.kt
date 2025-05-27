package com.example.climo.alerts.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.climo.data.local.ClimoDatabase
import com.example.climo.data.model.WeatherAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

class WeatherAlertsViewModel(private val database: ClimoDatabase) : ViewModel(){
    val activeAlerts: LiveData<List<WeatherAlert>> =
        database.weatherAlertDao().getActiveAlertsLiveData(
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )

    fun addAlert(alert: WeatherAlert){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    Log.d("WeatherAlertsViewModel" , "Inserting Alert: $alert")
                    database.weatherAlertDao().insert(alert)
                }
            }catch (e : Exception){
                Log.d("WeatherAlertsViewModel" , "Error adding Alert: ${e.message}")
            }
        }
    }
    fun deleteAlert(alert: WeatherAlert){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    Log.d("WeatherAlertsViewModel" , "Delete Alert: $alert")
                    database.weatherAlertDao().delete(alert)
                }
            }catch (e : Exception){
                Log.d("WeatherAlertsViewModel" , "Error deleting Alert: ${e.message}")
            }
        }
    }

    fun cleanupExpiredAlerts(currentDateTime: String){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    Log.d("WeatherAlertsViewModel" , "Cleanup Expired Alert: $currentDateTime")
                    database.weatherAlertDao().deleteExpiredAlerts(currentDateTime)
                }
            }catch (e : Exception){
                Log.d("WeatherAlertsViewModel" , "Error Cleanup Expired Alert: $currentDateTime")
            }
        }
    }


}