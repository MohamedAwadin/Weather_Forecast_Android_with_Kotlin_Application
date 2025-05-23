package com.example.climo.data.model

import android.health.connect.datatypes.units.Percentage
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.transition.Visibility


@Entity(tableName = "weather_data")
data class WeatherData (
    @PrimaryKey
    val locationId : String,
    val latitude: Double,
    val longitude: Double,
    val currentTemp : Double,
    val weatherDescription : String,
    val weatherIcon : String,
    val dateTime : Long,
    val pressure : Int ,
    val humidity : Int,
    val windSpeed : Double ,
    val cloudPercentage: Int ,
    val visibility: Int ,
    val hourlyForecast: String, // JSON string
    val dailyForecast: String  //  JSON string

)