package com.example.climo.data.remote

import com.google.gson.annotations.SerializedName




data class HourlyForecastResponse(
    @SerializedName("list") val hourly: List<HourlyForecast>
)

data class HourlyForecast(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: _Main,
    @SerializedName("weather") val weather: List<Weather>
)

data class _Main(
    @SerializedName("temp") val temp: Double
)

