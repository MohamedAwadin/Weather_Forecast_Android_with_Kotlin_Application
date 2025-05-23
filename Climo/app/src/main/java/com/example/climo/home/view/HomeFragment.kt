package com.example.climo.home.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.climo.data.local.ClimoDatabase
import com.example.climo.databinding.FragmentHomeBinding
import com.example.climo.home.viewmodel.HomeViewModel
import com.example.climo.home.viewmodel.HomeViewModelFactory
import com.github.matteobattilana.weather.PrecipType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(ClimoDatabase.getDatabase(requireContext()))
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fetchWeatherData()
            } else {
                showPermissionRequestCard()
                Toast.makeText(
                    requireContext(),
                    "Location permission denied. Please allow to display weather data.",
                    Toast.LENGTH_LONG
                ).show()
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

        binding.allowButton.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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

        viewModel.fetchWeather(lat, lon, "metric")

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
                showWeatherData()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.currentWeatherCard.visibility =
                if (isLoading == true) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}