package com.example.climo.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.climo.data.local.ClimoDatabase
import com.example.climo.data.repository.WeatherRepository

class HomeViewModelFactory(private val database: ClimoDatabase): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(WeatherRepository(database)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}