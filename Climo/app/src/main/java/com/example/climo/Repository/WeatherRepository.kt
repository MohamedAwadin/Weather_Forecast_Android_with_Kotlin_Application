package com.example.climo.Repository

import com.example.climo.Server.ApiServices

class WeatherRepository(val api: ApiServices) {
    fun getCurrentWeather(lat: Double, lng: Double, unit: String) =
        api.getCurrentWeather(lat, lng, unit, ApiKey = "d783c757a5dc7a74422b49da360f365e")

    fun getForecastWeather(lat: Double, lng: Double, unit: String) =
        api.getForecastWeather(lat, lng, unit, ApiKey = "d783c757a5dc7a74422b49da360f365e")
}