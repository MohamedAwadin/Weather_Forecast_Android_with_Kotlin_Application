package com.example.climo.home.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.climo.R
import com.example.climo.data.local.ClimoDatabase
import com.example.climo.data.model.WeatherData
import com.example.climo.databinding.FragmentHomeBinding
import com.example.climo.home.viewmodel.HomeViewModel
import com.example.climo.home.viewmodel.HomeViewModelFactory
import com.example.climo.settings.view.SettingsFragment
import com.github.matteobattilana.weather.PrecipType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(ClimoDatabase.getDatabase(requireContext()), requireContext())
    }
    private lateinit var hourlyAdapter: HourlyForecastAdapter
    private lateinit var dailyAdapter: DailyForecastAdapter

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fetchWeatherData()
            } else {
                showPermissionRequestCard()
                Toast.makeText(
                    requireContext(), getString(R.string.location_permission_denied), Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hourlyAdapter = HourlyForecastAdapter()
        dailyAdapter = DailyForecastAdapter()
        binding.hourlyForecastRecyclerView.adapter = hourlyAdapter
        binding.dailyForecastRecyclerView.adapter = dailyAdapter

        binding.allowButton.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        SettingsFragment.languageChangeTrigger.observe(viewLifecycleOwner) {
            fetchWeatherData()
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val locationChoice = sharedPreferences.getString("initial_location_choice", "gps")

        if (locationChoice == "gps" && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showPermissionRequestCard()
        } else {
            fetchWeatherData()
        }
    }

    private fun showPermissionRequestCard() {
        binding.permissionRequestCard.visibility = View.VISIBLE
        binding.weatherView.visibility = View.GONE
        binding.currentWeatherCard.visibility = View.GONE
        binding.hourlyForecastRecyclerView.visibility = View.GONE
        binding.dailyForecastRecyclerView.visibility = View.GONE
        binding.weatherDetailsCard.visibility = View.GONE
    }

    private fun showWeatherData() {
        binding.permissionRequestCard.visibility = View.GONE
        binding.weatherView.visibility = View.VISIBLE
        binding.currentWeatherCard.visibility = View.VISIBLE
        binding.hourlyForecastRecyclerView.visibility = View.VISIBLE
        binding.dailyForecastRecyclerView.visibility = View.VISIBLE
        binding.weatherDetailsCard.visibility = View.VISIBLE
    }

    private fun fetchWeatherData() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val lat = sharedPreferences.getFloat("selected_latitude", 37.7749f).toDouble()
        val lon = sharedPreferences.getFloat("selected_longitude", -122.4194f).toDouble()
        val locationName = sharedPreferences.getString("selected_location_name", "Unknown Location")
        binding.locationName.text = locationName

        viewModel.fetchWeather(lat, lon, )

        viewModel.weatherData.observe(viewLifecycleOwner) { weatherData ->
            weatherData?.let {
                updateWeatherUI(it)
                showWeatherData()
            }
        }

        viewModel.hourlyForecast.observe(viewLifecycleOwner) { hourlyForecasts ->
            Log.d("HomeFragment", "Hourly forecasts received: $hourlyForecasts")
            hourlyForecasts?.let { hourlyAdapter.submitList(it) }
        }

        viewModel.dailyForecast.observe(viewLifecycleOwner) { dailyForecasts ->
            Log.d("HomeFragment", "Daily forecasts received: $dailyForecasts")
            dailyForecasts?.let { dailyAdapter.submitList(it) }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.currentWeatherCard.visibility =
                if (isLoading == true) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        }
    }

    private fun updateWeatherUI(weatherData: WeatherData) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val tempUnit = sharedPreferences.getString("temp_unit", "metric") ?: "metric"
        val windSpeedUnit = sharedPreferences.getString("wind_speed_unit", "mps") ?: "mps"

        binding.currentTemp.text = when (tempUnit) {
            "metric" -> "${weatherData.currentTemp}°C"
            "imperial" -> "${weatherData.currentTemp}°F"
            "standard" -> "${weatherData.currentTemp}K"
            else -> "${weatherData.currentTemp}°C"
        }
        binding.weatherDescription.text = weatherData.weatherDescription
        binding.dateTime.text =
            SimpleDateFormat(
                getString(R.string.date_format),
                Locale.getDefault()
            ).format(Date(weatherData.dateTime * 1000))
        binding.pressure.text = getString(R.string.pressure, weatherData.pressure)
        binding.humidity.text = getString(R.string.humidity, weatherData.humidity)
        binding.windSpeed.text = getString(
            R.string.wind_speed,
            weatherData.windSpeed,
            if (windSpeedUnit == "mps") getString(R.string.unit_mps) else getString(R.string.unit_mph)
        )
        binding.clouds.text = getString(R.string.clouds, weatherData.cloudPercentage)
        binding.visibility.text = getString(R.string.visibility, weatherData.visibility)

        val iconRes = when (weatherData.weatherIcon) {
            "01d", "01n" -> R.drawable.sunny
            "02d", "02n" -> R.drawable.cloudy_sunny
            "03d", "03n", "04d", "04n" -> R.drawable.cloudy
            "09d", "09n", "10d", "10n" -> R.drawable.rainy
            "11d", "11n" -> R.drawable.storm
            "13d", "13n" -> R.drawable.snowy
            "50d", "50n" -> R.drawable.ic_haze
            else -> R.drawable.ic_warning
        }
        binding.currentWeatherIcon.setImageResource(iconRes)

        when (weatherData.weatherDescription.lowercase()) {
            "clear sky", "سماء صافية" -> binding.weatherView.apply {
                setWeatherData(PrecipType.CLEAR)
                angle = -20
                emissionRate = 100.0F
            }
            "light rain", "moderate rain", "مطر خفيف", "مطر معتدل" -> binding.weatherView.apply {
                setWeatherData(PrecipType.RAIN)
                angle = -20
                emissionRate = 100.0F
            }
            "snow", "ثلج" -> binding.weatherView.apply {
                setWeatherData(PrecipType.SNOW)
                angle = -20
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}