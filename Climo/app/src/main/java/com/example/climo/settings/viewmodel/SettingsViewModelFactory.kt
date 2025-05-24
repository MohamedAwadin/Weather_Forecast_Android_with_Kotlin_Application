package com.example.climo.settings.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient

class SettingsViewModelFactory(private val sharedPreferences: SharedPreferences , private val fusedLocationProviderClient: FusedLocationProviderClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(sharedPreferences, fusedLocationProviderClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}