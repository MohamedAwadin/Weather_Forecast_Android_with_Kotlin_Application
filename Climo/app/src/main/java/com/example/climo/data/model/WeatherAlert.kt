package com.example.climo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_alerts")
data class WeatherAlert (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0 ,
    val cityName: String,
    val latitude : Double,
    val longitude: Double,
    val fromDateTime: Long,
    val toDateTime: Long,
    val alertType: String
)