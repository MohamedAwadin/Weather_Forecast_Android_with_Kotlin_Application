package com.example.climo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.climo.data.model.WeatherData


@Dao
interface WeatherDataDao {
    @Insert
    suspend fun insert(weatherData: WeatherData)

    @Query("SELECT * FROM weather_data WHERE locationId = :locationId")
    suspend fun getWeatherData(locationId: String): WeatherData?

    @Query("DELETE FROM weather_data WHERE locationId = :locationId")
    suspend fun delete(locationId: String)
}