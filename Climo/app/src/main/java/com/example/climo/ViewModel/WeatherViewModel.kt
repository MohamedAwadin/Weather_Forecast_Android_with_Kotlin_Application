package com.example.climo.ViewModel

import androidx.lifecycle.ViewModel
import com.example.climo.Repository.WeatherRepository
import com.example.climo.Server.ApiClient
import com.example.climo.Server.ApiServices

class WeatherViewModel(val repository: WeatherRepository) : ViewModel() {
    constructor() : this(WeatherRepository(ApiClient().getClient().create(ApiServices::class.java)))

    fun loadCurrentWeather(lat: Double, lng: Double, unit: String) =
        repository.getCurrentWeather(lat, lng, unit)

    fun loadForecastWeather(lat: Double, lng: Double, unit: String) =
        repository.getForecastWeather(lat, lng, unit)
}