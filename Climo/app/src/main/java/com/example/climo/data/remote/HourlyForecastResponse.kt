package com.example.climo.data.remote

data class HourlyForecastResponse(
    val hourly: List<HourlyForecast>
)

data class HourlyForecast (
    val dt: Long,
    val temp: Double,
    val weather: List<Weather>
)

