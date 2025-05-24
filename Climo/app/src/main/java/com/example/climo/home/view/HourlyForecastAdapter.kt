package com.example.climo.home.view

import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.climo.R
import com.example.climo.data.remote.HourlyForecast
import com.example.climo.databinding.ItemHourlyForecastBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HourlyForecastAdapter : RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder>() {

    private var hourlyForecasts: List<HourlyForecast> = emptyList()

    fun submitList(forecasts: List<HourlyForecast>) {
        hourlyForecasts = forecasts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val binding = ItemHourlyForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HourlyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        holder.bind(hourlyForecasts[position])
    }

    override fun getItemCount(): Int = hourlyForecasts.size

    class HourlyViewHolder(private val binding: ItemHourlyForecastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(forecast: HourlyForecast) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
            val tempUnit = sharedPreferences.getString("temp_unit", "metric") ?: "metric"

            val timeFormat = SimpleDateFormat(
                binding.root.context.getString(R.string.time_format),
                Locale.getDefault()
            )
            binding.hourlyTime.text = timeFormat.format(Date(forecast.dt * 1000))
            binding.hourlyTemperature.text = when (tempUnit) {
                "metric" -> binding.root.context.getString(R.string.temp_celsius_single, forecast.main.temp)
                "imperial" -> binding.root.context.getString(R.string.temp_fahrenheit_single, forecast.main.temp)
                "standard" -> binding.root.context.getString(R.string.temp_kelvin_single, forecast.main.temp)
                else -> binding.root.context.getString(R.string.temp_celsius_single, forecast.main.temp)
            }

            val iconRes = when (forecast.weather.firstOrNull()?.icon) {
                "01d", "01n" -> R.drawable.sunny
                "02d", "02n" -> R.drawable.cloudy_sunny
                "03d", "03n", "04d", "04n" -> R.drawable.cloudy
                "09d", "09n", "10d", "10n" -> R.drawable.rainy
                "11d", "11n" -> R.drawable.storm
                "13d", "13n" -> R.drawable.snowy
                "50d", "50n" -> R.drawable.ic_haze
                else -> R.drawable.ic_warning
            }
            binding.hourlyWeatherIcon.setImageResource(iconRes)
        }
    }
}