package com.example.climo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.climo.data.model.WeatherAlert

@Dao
interface WeatherAlertDao {

    @Insert
    suspend fun insert(alert: WeatherAlert)

    @Query("SELECT * FROM weather_alerts WHERE toDateTime > :currentTime")
    suspend fun getActiveAlerts(currentTime: Long): List<WeatherAlert>

    @Delete
    suspend fun delete(alert: WeatherAlert)

}