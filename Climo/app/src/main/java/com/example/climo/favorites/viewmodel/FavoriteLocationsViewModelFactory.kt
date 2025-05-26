package com.example.climo.favorites.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.climo.data.local.ClimoDatabase

class FavoriteLocationsViewModelFactory(private val database: ClimoDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteLocationsViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return FavoriteLocationsViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}