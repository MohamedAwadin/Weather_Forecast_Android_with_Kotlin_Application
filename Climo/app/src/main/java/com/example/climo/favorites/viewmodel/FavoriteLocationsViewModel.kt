package com.example.climo.favorites.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.climo.R
import com.example.climo.data.local.ClimoDatabase
import com.example.climo.data.model.FavoriteLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteLocationsViewModel(private val database: ClimoDatabase): ViewModel() {

    val favoriteLocations : LiveData<List<FavoriteLocation>>  = database.favoriteLocationDao().getAllFavoritesLiveData()

    fun addFavoriteLocation(latitude: Double, longitude: Double, cityName: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    Log.e("FavoriteLocationsViewModel", "addFavoriteLocation: $latitude, $longitude, $cityName")
                    database.favoriteLocationDao().insert(
                        FavoriteLocation(
                            latitude = latitude,
                            longitude = longitude,
                            cityName = cityName
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("FavoriteLocationsViewModel", "Error adding favorite location: ${e.message}")
            }
        }
    }

    fun deleteFavoriteLocation(location: FavoriteLocation) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    database.favoriteLocationDao().delete(location)
                }
            } catch (e: Exception) {
                Log.e("FavoriteLocationsViewModel", "Error deleting favorite location: ${e.message}")
            }
        }
    }

//    fun addFavoriteLocation(latitude: Double ,longitude : Double , cityName: String){
//        viewModelScope.launch(Dispatchers.IO) {
//            Log.e("FavoriteLocationsViewModel" , "addFavoriteLocation: ${latitude} , ${longitude}, ${cityName}")
//            val location = FavoriteLocation(
//                id = 0,
//                latitude = latitude,
//                longitude = longitude,
//                cityName = cityName
//            )
//            database.favoriteLocationDao().insert(location)
//        }
//    }
//
//    fun deleteFavoriteLocation(location: FavoriteLocation){
//        viewModelScope.launch(Dispatchers.IO) {
//            database.favoriteLocationDao().delete(location)
//        }
//    }


}