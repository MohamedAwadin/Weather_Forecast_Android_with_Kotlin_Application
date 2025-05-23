package com.example.climo.data.repository


import androidx.core.content.ContextCompat.getString
import com.example.climo.R
import com.example.climo.data.local.ClimoDatabase
import com.example.climo.data.model.WeatherData
import com.example.climo.data.remote.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher

class WeatherRepository(private val database: ClimoDatabase) {
    val api_key : String = R.string.OPEN_WEATHER_API_KEY.toString()
    suspend fun getWeatherData(lat: Double , lon: Double , units: String): WeatherData?{
        val locationId = "${lat}_${lon}"
        return withContext(Dispatchers.IO){
            try {
                if (isNetworkAvailable()){
                    val currentWeather = RetrofitClient.api.getCurrentWeather(lat, lon, "ecfe2681690524ece36e0e4818523e5f", units)
                    val hourlyForecast = RetrofitClient.api.getHourlyForecast(lat, lon, "ecfe2681690524ece36e0e4818523e5f", units)
                    val dailyForecast = RetrofitClient.api.getDailyForecast(lat, lon, 16, "ecfe2681690524ece36e0e4818523e5f", units)

                    val weatherData = WeatherData(
                        locationId= locationId,
                        latitude = lat,
                        longitude = lon,
                        currentTemp = currentWeather.main.temp,
                        weatherDescription = currentWeather.weather.first().description,
                        weatherIcon = currentWeather.weather.first().icon,
                        dateTime = currentWeather.dt,
                        pressure = currentWeather.main.pressure,
                        humidity = currentWeather.main.humidity,
                        windSpeed = currentWeather.wind.speed,
                        cloudPercentage = currentWeather.clouds.all,
                        visibility = currentWeather.visibility,
                        hourlyForecast = Gson().toJson(hourlyForecast.hourly),
                        dailyForecast = Gson().toJson(dailyForecast.daily)
                    )

                    database.weatherDataDao().delete(locationId)
                    database.weatherDataDao().insert(weatherData)
                    weatherData

                }else{
                    database.weatherDataDao().getWeatherData(locationId)
                }

            }catch (e : Exception){
                database.weatherDataDao().getWeatherData(locationId)
            }

        }

    }
    private fun isNetworkAvailable(): Boolean {

        return true
    }
}