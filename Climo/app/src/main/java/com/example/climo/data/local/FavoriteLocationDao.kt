package com.example.climo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.climo.data.model.FavoriteLocation

@Dao
interface FavoriteLocationDao {

    @Insert
    suspend fun insert(location: FavoriteLocation)

    @Query("SELECT * FROM favorite_locations")
    suspend fun getAllFavorites(): List<FavoriteLocation>

    @Delete
    suspend fun delete(location: FavoriteLocation)

}