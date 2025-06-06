package com.example.climo.alerts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.climo.data.local.ClimoDatabase
import com.example.climo.data.repository.WeatherAlertRepository

class WeatherAlertsViewModelFactory(private val database: ClimoDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherAlertsViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return WeatherAlertsViewModel(WeatherAlertRepository(database.weatherAlertDao())) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}