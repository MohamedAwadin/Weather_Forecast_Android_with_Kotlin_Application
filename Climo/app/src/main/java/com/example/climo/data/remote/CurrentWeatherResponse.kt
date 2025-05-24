package com.example.climo.data.remote

import androidx.transition.Visibility
import com.google.gson.annotations.SerializedName

data class CurrentWeatherResponse(
    @SerializedName("main") val main: Main,
    @SerializedName("weather")val weather: List<Weather>,
    @SerializedName("wind")val wind: Wind,
    @SerializedName("clouds")val clouds: Clouds,
    @SerializedName("visibility")val visibility: Int,
    @SerializedName("dt")val dt: Long
)

data class Clouds (
    @SerializedName("all") val all : Int
)

data class Wind (
    @SerializedName("speed") val speed: Double
)

data class Weather(
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class Main (
    @SerializedName("temp") val temp: Double,
    @SerializedName("pressure") val pressure : Int,
    @SerializedName("humidity") val humidity: Int
)
