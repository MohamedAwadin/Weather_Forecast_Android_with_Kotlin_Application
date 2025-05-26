package com.example.climo.settings.viewmodel

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.example.climo.data.remote.RetrofitClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(private val sharedPreferences: SharedPreferences , private val fusedLocationProviderClient: FusedLocationProviderClient) : ViewModel() {


    fun saveLocationChoice(choice: String) {
        sharedPreferences.edit { putString("initial_location_choice", choice) }
    }

    fun saveLocation(latitude: Double, longitude: Double, locationName: String) {
        sharedPreferences.edit {
            putFloat("current_latitude", latitude.toFloat())
            putFloat("current_longitude", longitude.toFloat())
            putString("current_location_name", locationName)
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchGpsLocation(fusedLocationProviderClient: FusedLocationProviderClient){
        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null){

                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val response = withContext(Dispatchers.IO) {
                                RetrofitClient.api.getReverseGeocoding(
                                    location.latitude,
                                    location.longitude,
                                    1,
                                    "ecfe2681690524ece36e0e4818523e5f"
                                )
                            }
                            val locationName = if (response.isNotEmpty()) {
                                response[0].name
                            } else {
                                "Unknown Location"
                            }
                            saveLocation(location.latitude , location.longitude , locationName)

                        }catch (e: Exception){
                            saveLocation(location.latitude, location.longitude, "Unknown Location")
                            Log.d("SettingViewModel" , "Couldn't fetch city name")

                        }
                    }
                }
            }

    }

    fun saveLanguage(language: String) {
        sharedPreferences.edit { putString("language", language) }
    }

    fun getLanguage(): String {
        return sharedPreferences.getString("language", "en") ?: "en"
    }

    fun saveTempUnit(unit: String) {
        sharedPreferences.edit { putString("temp_unit", unit) }
    }

    fun getTempUnit(): String {
        return sharedPreferences.getString("temp_unit", "metric") ?: "metric"
    }

    fun saveWindSpeedUnit(unit: String) {
        sharedPreferences.edit { putString("wind_speed_unit", unit) }
    }

    fun getWindSpeedUnit(): String {
        return sharedPreferences.getString("wind_speed_unit", "mps") ?: "mps"
    }




}