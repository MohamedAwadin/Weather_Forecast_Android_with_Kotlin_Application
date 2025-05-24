package com.example.climo.home.view

import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.climo.R
import com.example.climo.data.remote.DailyForecast
import com.example.climo.databinding.ItemDailyForecastBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyForecastAdapter : RecyclerView.Adapter<DailyForecastAdapter.DailyViewHolder>() {

    private var dailyForecasts: List<DailyForecast> = emptyList()

    fun submitList(forecasts: List<DailyForecast>) {
        dailyForecasts = forecasts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val binding = ItemDailyForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DailyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        holder.bind(dailyForecasts[position])
    }

    override fun getItemCount(): Int = dailyForecasts.size

    class DailyViewHolder(private val binding: ItemDailyForecastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(forecast: DailyForecast) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
            val tempUnit = sharedPreferences.getString("temp_unit", "metric") ?: "metric"

            val date = Date(forecast.dt * 1000)
            val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            binding.dailyDayName.text = dateFormat.format(date)

            binding.dailyWeatherDescription.text = forecast.weather.firstOrNull()?.description ?: binding.root.context.getString(R.string.unknown)
            binding.dailyTemp.text = when (tempUnit) {
                "metric" -> binding.root.context.getString(R.string.temp_celsius, forecast.temp.min, forecast.temp.max)
                "imperial" -> binding.root.context.getString(R.string.temp_fahrenheit, forecast.temp.min, forecast.temp.max)
                "standard" -> binding.root.context.getString(R.string.temp_kelvin, forecast.temp.min, forecast.temp.max)
                else -> binding.root.context.getString(R.string.temp_celsius, forecast.temp.min, forecast.temp.max)
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
            binding.dailyWeatherIcon.setImageResource(iconRes)
        }
    }
}