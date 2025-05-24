package com.example.climo.Activity

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.climo.R
import com.example.climo.data.remote.RetrofitClient
import com.example.climo.databinding.ActivityMainBinding
import com.example.climo.databinding.DialogInitialSetupBinding
import com.example.climo.Activity.MapSelectionActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.settingsFragment)
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)

        if (!sharedPreferences.getBoolean("setup_completed", false)) {
            showInitialDialog()
        }

//        try {
//            val navHostFragment =
//                supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment
//            val navController = navHostFragment.navController
//            setupActionBarWithNavController(navController)
//            Log.d("MainActivity", "NavController setup successful")
//        } catch (e: Exception) {
//            Log.e("MainActivity", "Error setting up NavController: $e")
//        }
    }

    private fun showInitialDialog() {
        val dialogBinding = DialogInitialSetupBinding.inflate(layoutInflater)
        val dialogView = dialogBinding.root

        MaterialAlertDialogBuilder(this)
            .setTitle("Initial Setup")
            .setView(dialogView)
            .setPositiveButton("Ok") { _, _ ->
                val locationChoice = when {
                    dialogBinding.gpsRadioButton.isChecked -> "gps"
                    dialogBinding.mapRadioButton.isChecked -> "map"
                    else -> "gps"
                }
                val notificationChoice = when {
                    dialogBinding.enableNotificationsRadioButton.isChecked -> "enable"
                    dialogBinding.disableNotificationsRadioButton.isChecked -> "disable"
                    else -> "disable"
                }
                with(sharedPreferences.edit()) {
                    putBoolean("setup_completed", true)
                    putString("initial_location_choice", locationChoice)
                    putString("initial_notification_choice", notificationChoice)
                    apply()
                }
                handlePermissions(locationChoice, notificationChoice)
            }
            .setCancelable(false)
            .show()

        dialogBinding.gpsRadioButton.isChecked = true
        dialogBinding.disableNotificationsRadioButton.isChecked = true
    }

    private fun handlePermissions(locationChoice: String, notificationChoice: String) {
        when (locationChoice) {
            "gps" -> requestLocationPermission()
            "map" -> {
                val intent = Intent(this, MapSelectionActivity::class.java)
                startActivityForResult(intent, REQUEST_MAP_SELECTION)
            }
        }
        if (notificationChoice == "enable") {
            requestNotificationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            fetchLocation()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    with(sharedPreferences.edit()) {
                        putFloat("selected_latitude", it.latitude.toFloat())
                        putFloat("selected_longitude", it.longitude.toFloat())
                        apply()
                    }
                    fetchLocationName(it.latitude, it.longitude)
                } ?: run {
                    Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                    navigateToHomeFragment()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to fetch location: ${it.message}", Toast.LENGTH_SHORT).show()
                navigateToHomeFragment()
            }
        }
    }

    private fun fetchLocationName(lat: Double, lon: Double) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getReverseGeocoding(lat, lon, 1, "ecfe2681690524ece36e0e4818523e5f")
                }
                if (response.isNotEmpty()) {
                    val locationName = response[0].name
                    with(sharedPreferences.edit()) {
                        putString("selected_location_name", locationName)
                        apply()
                    }
                }
                navigateToHomeFragment()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error fetching location name: ${e.message}", Toast.LENGTH_SHORT).show()
                navigateToHomeFragment()
            }
        }
    }

    private fun navigateToHomeFragment() {
        try {
            navController.navigate(R.id.homeFragment)
        } catch (e: Exception) {
            Log.e("MainActivity", "Navigation error: $e")
        }
//        try {
//            val navHostFragment =
//                supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment
//            navHostFragment.navController.navigate(R.id.homeFragment)
//        } catch (e: Exception) {
//            Log.e("MainActivity", "Navigation error: $e")
//        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLocation()
                } else {
                    // Let HomeFragment handle the permission request
                    navigateToHomeFragment()
                }
            }
            REQUEST_NOTIFICATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    with(sharedPreferences.edit()) {
                        putString("initial_notification_choice", "enable")
                        apply()
                    }
                } else {
                    with(sharedPreferences.edit()) {
                        putString("initial_notification_choice", "disable")
                        apply()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MAP_SELECTION && resultCode == RESULT_OK) {
            data?.let {
                val lat = it.getFloatExtra("selected_latitude", 37.7749f)
                val lon = it.getFloatExtra("selected_longitude", -122.4194f)
                with(sharedPreferences.edit()) {
                    putFloat("selected_latitude", lat)
                    putFloat("selected_longitude", lon)
                    apply()
                }
            }
            navigateToHomeFragment()
        } else {
            navigateToHomeFragment()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return try {
            navController.navigateUp() || super.onSupportNavigateUp()
        } catch (e: IllegalStateException) {
            Log.e("MainActivity", "NavigateUp: $e")
            super.onSupportNavigateUp()
        }
//        return try {
//            val navHostFragment =
//                supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment
//            navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
//        } catch (e: IllegalStateException) {
//            Log.e("MainActivity", "NavigateUp: $e")
//            super.onSupportNavigateUp()
//        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
        private const val REQUEST_NOTIFICATION_PERMISSION = 101
        private const val REQUEST_MAP_SELECTION = 102
    }
}