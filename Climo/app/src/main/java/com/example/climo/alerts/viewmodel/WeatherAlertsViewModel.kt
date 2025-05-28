package com.example.climo.alerts.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.climo.data.model.WeatherAlert
import com.example.climo.data.repository.WeatherAlertRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherAlertsViewModel(private val repository: WeatherAlertRepository) : ViewModel() {

    val activeAlerts: LiveData<List<WeatherAlert>> = repository.getActiveAlertsLiveData(
        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )

    fun addAlert(alert: WeatherAlert) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    Log.d("WeatherAlertsViewModel", "Inserting Alert: $alert")
                    repository.insertAlert(alert)
                }
            } catch (e: Exception) {
                Log.d("WeatherAlertsViewModel", "Error adding Alert: ${e.message}")
            }
        }
    }

    fun deleteAlert(alert: WeatherAlert) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    Log.d("WeatherAlertsViewModel", "Deleting Alert: $alert.id")
                    repository.deleteAlert(alert)
                }
            } catch (e: Exception) {
                Log.d("WeatherAlertsViewModel", "Error deleting Alert: ${e.message}")
            }
        }
    }

    fun cleanupExpiredAlerts() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    Log.d("WeatherAlertsViewModel", "Cleaning up expired alerts: $currentDateTime")
                    repository.deleteExpiredAlerts(currentDateTime)
                }
            } catch (e: Exception) {
                Log.d("WeatherAlertsViewModel", "Error cleaning up expired alerts: ${e.message}")
            }
        }
    }
}