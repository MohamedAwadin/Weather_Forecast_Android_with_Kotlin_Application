package com.example.climo.data.remote

import com.google.gson.annotations.SerializedName

//data class DailyForecastResponse(
//    val daily  : List<DailyForecast>
//)
//
//data class DailyForecast (
//    val dt: Long,
//    val temp: Temp,
//    val weather: List<Weather>
//)
//
//
//data class Temp(
//    val max: Double,
//    val min: Double
//)

data class DailyForecastResponse(
    @SerializedName("list") val daily: List<DailyForecast>
)

data class DailyForecast(
    @SerializedName("dt") val dt: Long,
    @SerializedName("temp") val temp: Temp,
    @SerializedName("weather") val weather: List<Weather>
)

data class Temp(
    @SerializedName("min") val min: Double,
    @SerializedName("max") val max: Double
)

//data class Weather(
//    @SerializedName("description") val description: String,
//    @SerializedName("icon") val icon: String
//)