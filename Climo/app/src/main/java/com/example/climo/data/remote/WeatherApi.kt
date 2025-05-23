package com.example.climo.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): CurrentWeatherResponse

    @GET("forecast/hourly")
    suspend fun getHourlyForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): HourlyForecastResponse

    @GET("forecast/daily")
    suspend fun getDailyForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("cnt") count: Int = 16,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): DailyForecastResponse

    @GET("geo/1.0/direct")
    suspend fun getGeocoding(
        @Query("q") cityName: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String
    ): List<GeocodingResponse>
}