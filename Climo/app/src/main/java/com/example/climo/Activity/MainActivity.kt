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
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.climo.R
import com.example.climo.data.remote.RetrofitClient
import com.example.climo.databinding.ActivityMainBinding
import com.example.climo.databinding.DialogInitialSetupBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.favoriteLocationsFragment, R.id.settingsFragment),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            Log.d("MainActivity", "Navigation item clicked: ${menuItem.itemId}")
            val handled = when (menuItem.itemId) {
                R.id.favoriteLocationsFragment -> {
                    navController.navigate(R.id.favoriteLocationsFragment)
                    true
                }
                else -> navController.navigate(menuItem.itemId)
            }
            Log.d("MainActivity", "Navigation handled: $handled")
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        if (!sharedPreferences.getBoolean("setup_complete", false)) {
            showInitialDialog()

        } else {
            navigateToHomeFragment()
        }
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
                    putBoolean("setup_complete", true)
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
                intent.putExtra("from_where" , "home")
                startActivityForResult(intent, REQUEST_MAP_SELECTION)
            }
        }
        if (notificationChoice == "enable") {
            requestNotificationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            fetchLocation()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    with(sharedPreferences.edit()) {
                        putFloat("current_latitude", it.latitude.toFloat())
                        putFloat("current_longitude", it.longitude.toFloat())
                        apply()
                    }
                    fetchLocationName(it.latitude, it.longitude)
                } ?: run {
                    Toast.makeText(this, getString(R.string.unable_to_fetch_location), Toast.LENGTH_SHORT).show()
                    navigateToHomeFragment()
                }
            }.addOnFailureListener {
                Toast.makeText(this, getString(R.string.failed_to_fetch_location, it.message), Toast.LENGTH_SHORT).show()
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
                        putString("current_location_name", locationName)
                        apply()
                    }
                }
                navigateToHomeFragment()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, getString(R.string.error_fetching_location_name, e.message), Toast.LENGTH_SHORT).show()
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
                    navigateToHomeFragment()
                }
            }
            REQUEST_NOTIFICATION_PERMISSION -> {
                with(sharedPreferences.edit()) {
                    putString(
                        "initial_notification_choice",
                        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) "enable" else "disable"
                    )
                    apply()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MAP_SELECTION && resultCode == RESULT_OK && sharedPreferences.getBoolean("setup_complete", false)) {
//            data?.let {
//                val lat = it.getFloatExtra("current_latitude", 37.7749f)
//                val lon = it.getFloatExtra("current_longitude", -122.4194f)
//                val locationName = it.getStringExtra("current_location_name") ?: "Unknown Location"
//                with(sharedPreferences.edit()) {
//                    putFloat("current_latitude", lat)
//                    putFloat("current_longitude", lon)
//                    putString("current_location_name", locationName)
//                    apply()
//                }
//            }
//            navigateToHomeFragment()
//        }
            data?.let {
                val lat = it.getFloatExtra("current_latitude", 37.7749f)
                val lon = it.getFloatExtra("current_longitude", -122.4194f)
                val locationName = it.getStringExtra("current_location_name") ?: "Unknown Location"
                with(sharedPreferences.edit()) {
                    putFloat("current_latitude", lat)
                    putFloat("current_longitude", lon)
                    putString("current_location_name", locationName)
                    apply()
                }
                navigateToHomeFragment()
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
        private const val REQUEST_NOTIFICATION_PERMISSION = 101
        private const val REQUEST_MAP_SELECTION = 102
    }
}