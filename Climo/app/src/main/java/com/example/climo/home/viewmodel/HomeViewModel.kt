package com.example.climo.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.climo.data.model.WeatherData
import com.example.climo.data.repository.WeatherRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: WeatherRepository): ViewModel() {

    private val _weatherData = MutableLiveData<WeatherData?>()
    val weatherData: LiveData<WeatherData?> get() = _weatherData


    private val _isLoading = MutableLiveData<Boolean?>()
    val isLoading: LiveData<Boolean?> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun fetchWeather(lat: Double , lon: Double, units: String){
        viewModelScope.launch {
            _isLoading.value=true
            _error.value=null

            try {
                val data = repository.getWeatherData(lat , lon, units)
                _weatherData.value =data
                if (data == null){
                    _error.value = "No data available"
                }
            }catch (e: Exception){
                _error.value = "Error fetching weather: ${e.message}"
            }finally {
                _isLoading.value = false
            }
        }
    }
}