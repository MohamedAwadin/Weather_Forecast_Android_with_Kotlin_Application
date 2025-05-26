package com.example.climo.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorite_locations",
    indices = [Index(value = ["latitude", "longitude", "cityName"], unique = true)]
)
data class FavoriteLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0 ,
    val cityName: String,
    val latitude : Double,
    val longitude: Double

)