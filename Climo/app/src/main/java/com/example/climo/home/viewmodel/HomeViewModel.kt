package com.example.climo.home.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.climo.data.model.WeatherData
import com.example.climo.data.remote.DailyForecast
import com.example.climo.data.remote.HourlyForecast
import com.example.climo.data.repository.WeatherRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: WeatherRepository,
    private val context: Context
) : ViewModel() {

    private val _weatherData = MutableLiveData<WeatherData?>()
    val weatherData: LiveData<WeatherData?> get() = _weatherData

    private val _hourlyForecast = MutableLiveData<List<HourlyForecast>?>()
    val hourlyForecast: LiveData<List<HourlyForecast>?> get() = _hourlyForecast

    private val _dailyForecast = MutableLiveData<List<DailyForecast>?>()
    val dailyForecast: LiveData<List<DailyForecast>?> get() = _dailyForecast

    private val _isLoading = MutableLiveData<Boolean?>()
    val isLoading: LiveData<Boolean?> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val data = repository.getWeatherData(lat, lon)
                _weatherData.value = data
                if (data == null) {
                    _error.value = "No data available"
                    Log.e("HomeViewModel", "Weather data is null")
                } else {
                    try {
                        val hourlyType = object : TypeToken<List<HourlyForecast>>() {}.type
                        val dailyType = object : TypeToken<List<DailyForecast>>() {}.type
                        val hourlyList = Gson().fromJson<List<HourlyForecast>>(data.hourlyForecast, hourlyType)
                        val dailyList = Gson().fromJson<List<DailyForecast>>(data.dailyForecast, dailyType)

                        Log.d("HomeViewModel", "Parsed hourly forecast: $hourlyList")
                        Log.d("HomeViewModel", "Parsed daily forecast: $dailyList")

                        _hourlyForecast.value = hourlyList ?: emptyList()
                        _dailyForecast.value = dailyList ?: emptyList()
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Error parsing forecast JSON: ${e.message}", e)
                        _hourlyForecast.value = emptyList()
                        _dailyForecast.value = emptyList()
                        _error.value = "Error parsing forecast data"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error fetching weather: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

