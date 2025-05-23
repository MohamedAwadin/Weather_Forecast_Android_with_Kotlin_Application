package com.example.climo.data.remote

data class DailyForecastResponse(
    val daily  : List<DailyForecast>
)

data class DailyForecast (
    val dt: Long,
    val temp: Temp,
    val weather: List<Weather>
)


data class Temp(
    val max: Double,
    val min: Double
)