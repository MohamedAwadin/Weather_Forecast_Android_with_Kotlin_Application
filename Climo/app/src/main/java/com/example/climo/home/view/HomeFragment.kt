package com.example.climo.home.view

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.viewModels
import com.example.climo.R
import com.example.climo.data.local.ClimoDatabase
import com.example.climo.databinding.FragmentHomeBinding
import com.example.climo.home.viewmodel.HomeViewModel
import com.example.climo.home.viewmodel.HomeViewModelFactory
import com.github.matteobattilana.weather.PrecipType
import okhttp3.internal.http2.Header
import okio.Lock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(ClimoDatabase.getDatabase(requireContext()))
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

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val locationChoice = sharedPreferences.getString("initial_location_choice", "gps")

        if (locationChoice == "gps" && checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            binding.currentWeatherCard.visibility = View.GONE
            binding.hourlyForecastRecyclerView.visibility = View.GONE
            binding.dailyForecastRecyclerView.visibility = View.GONE
            binding.weatherDetailsCard.visibility = View.GONE
            binding.weatherView.visibility = View.GONE

            Toast.makeText(
                requireContext(),
                "We can't fetch your location, please allow Climo to access your location and [Allow]",
                Toast.LENGTH_LONG
            ).show()
            // Intent to request permission should be handled in MainActivity
        } else {
            // Fetch weather for a default location (e.g., user's location)
            viewModel.fetchWeather(37.7749, -122.4194, "metric") // Example: San Francisco

            viewModel.weatherData.observe(viewLifecycleOwner) { weatherData ->
                weatherData?.let {
                    binding.currentTemp.text = "${it.currentTemp}"
                    binding.weatherDescription.text = it.weatherDescription
                    binding.dateTime.text =
                        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(
                            Date(it.dateTime * 1000)
                        )
                    binding.pressure.text = "Pressure: ${it.pressure} hpa"
                    binding.humidity.text = "Humidity: ${it.humidity}%"
                    binding.windSpeed.text = "Wind Speed: ${it.windSpeed} m/s"
                    binding.clouds.text = "Clouds : ${it.cloudPercentage} %"
                    binding.visibility.text = "Visibility: ${it.visibility} m"

                    when (it.weatherDescription.lowercase()) {
                        "clear sky" -> binding.weatherView.apply {
                            setWeatherData(PrecipType.CLEAR)
                            angle = -20
                            emissionRate = 100.0F
                        }

                        "light rain", "moderate rain" -> binding.weatherView.apply {
                            setWeatherData(PrecipType.RAIN)
                            angle = -20
                            emissionRate = 100.0F
                        }

                        "snow" -> binding.weatherView.apply {
                            setWeatherData(PrecipType.SNOW)
                            angle = -20
                        }
                    }

                }

            }
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                binding.currentWeatherCard.visibility =
                    if (isLoading == true) View.GONE else View.VISIBLE
            }

            viewModel.error.observe(viewLifecycleOwner) { error ->
                error?.let { Toast.makeText(context, "hiiiiiii", Toast.LENGTH_LONG).show() }
            }
        }


    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

