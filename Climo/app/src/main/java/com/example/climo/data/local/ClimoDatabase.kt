package com.example.climo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.climo.data.model.FavoriteLocation
import com.example.climo.data.model.WeatherAlert
import com.example.climo.data.model.WeatherData

@Database(entities = [WeatherData::class, FavoriteLocation::class , WeatherAlert::class], version = 2, exportSchema = false)
abstract class ClimoDatabase : RoomDatabase() {
    abstract fun weatherDataDao(): WeatherDataDao
    abstract fun favoriteLocationDao(): FavoriteLocationDao
    abstract fun weatherAlertDao(): WeatherAlertDao

    companion object{
        @Volatile
        private var INSTANCE: ClimoDatabase?= null

        fun getDatabase(context: Context): ClimoDatabase{
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClimoDatabase::class.java,
                    "climo_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}