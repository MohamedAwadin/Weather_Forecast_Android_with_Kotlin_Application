package com.example.climo.data.remote

data class WeatherResponse(
    val weather: List<Weather_Desc>,
    val main: WeatherCondition
)

data class Weather_Desc(
    val description: String
)

data class WeatherCondition(
    val temp: Double
)