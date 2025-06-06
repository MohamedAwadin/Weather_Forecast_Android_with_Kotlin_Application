package com.example.climo.home.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.climo.data.local.ClimoDatabase
import com.example.climo.data.repository.WeatherRepository

class HomeViewModelFactory(
    private val database: ClimoDatabase,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(WeatherRepository(database, context), context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}