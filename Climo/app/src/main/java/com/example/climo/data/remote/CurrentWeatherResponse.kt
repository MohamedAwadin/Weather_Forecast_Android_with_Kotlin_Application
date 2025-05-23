package com.example.climo.data.remote

import androidx.transition.Visibility

data class CurrentWeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val clouds: Clouds,
    val visibility: Int,
    val dt: Long
)

data class Clouds (
    val all : Int
)

data class Wind (
    val speed: Double
)

data class Weather (
    val description : String,
    val icon : String
    )

data class Main (
    val temp: Double,
    val pressure : Int,
    val humidity: Int
)
