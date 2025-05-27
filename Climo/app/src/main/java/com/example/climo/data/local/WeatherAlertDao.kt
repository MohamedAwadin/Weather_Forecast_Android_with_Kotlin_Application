package com.example.climo.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.climo.data.model.WeatherAlert

@Dao
interface WeatherAlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: WeatherAlert): Long

    @Delete
    suspend fun delete(alert: WeatherAlert)

    @Query("SELECT * FROM weather_alerts WHERE toDateTime > :currentDateTime")
     fun getActiveAlertsLiveData(currentDateTime: String): LiveData<List<WeatherAlert>>

    @Query("DELETE FROM weather_alerts WHERE toDateTime <= :currentDateTime")
    suspend fun deleteExpiredAlerts(currentDateTime: String)

}