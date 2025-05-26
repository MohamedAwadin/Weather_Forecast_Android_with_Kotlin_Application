package com.example.climo.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.climo.data.model.FavoriteLocation

@Dao
interface FavoriteLocationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(location: FavoriteLocation)

    @Query("SELECT * FROM favorite_locations")
    suspend fun getAllFavorites(): List<FavoriteLocation>

    @Query("SELECT * FROM favorite_locations")
    fun getAllFavoritesLiveData(): LiveData<List<FavoriteLocation>>

    @Delete
    suspend fun delete(location: FavoriteLocation)

}