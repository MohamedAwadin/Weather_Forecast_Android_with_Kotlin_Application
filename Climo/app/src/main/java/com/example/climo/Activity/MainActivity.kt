package com.example.climo.Activity

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.work.impl.model.Preference
import com.example.climo.R
import com.example.climo.data.model.FavoriteLocation
import com.example.climo.databinding.ActivityMainBinding
import com.example.climo.databinding.DialogInitialSetupBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (!sharedPreferences.getBoolean("setup_complete", false)){
            showInitialDialog()
        }

        try {
            val navHostFragment = supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment
            val navController = navHostFragment.navController
            setupActionBarWithNavController(navController)
            Log.d("MainActivity", "NavController setup successful")
        }catch (e: Exception){
            Log.e("MainActivity", "Error setting up NavController: $e")
        }

    }

    private fun showInitialDialog(){
        val Dialog_binding = DialogInitialSetupBinding.inflate(layoutInflater)
        val dialogView= Dialog_binding.root

        MaterialAlertDialogBuilder(this)
            .setTitle("Initial Setup")
            .setView(dialogView)
            .setPositiveButton("Ok") { _ , _ ->
                val locationChoice = when{
                    Dialog_binding.gpsRadioButton.isChecked -> "gps"
                    Dialog_binding.mapRadioButton.isChecked -> "map"
                    else -> "gps"
                }
                val notificationChoice = when{
                    Dialog_binding.enableNotificationsRadioButton.isChecked -> "enable"
                    Dialog_binding.disableNotificationsRadioButton.isChecked -> "disable"
                    else -> "disable"
                }
                with(sharedPreferences.edit()) {
                    putBoolean("setup_completed", true)
                    putString("initial_location_choice", locationChoice)
                    putString("initial_notification_choice", notificationChoice)
                    apply()
                }
                handlePermissions(locationChoice , notificationChoice)
                try {
                    val navHostFragment = supportFragmentManager
                        .findFragmentById(binding.navHostFragment.id) as NavHostFragment
                    navHostFragment.navController.navigate(R.id.homeFragment)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Navigation error: $e")
                }
            }
            .setCancelable(false)
            .show()
        Dialog_binding.gpsRadioButton.isChecked = true
        Dialog_binding.disableNotificationsRadioButton.isChecked = true
    }

    private fun handlePermissions(locationChoice: String, notificationChoice: String){
        when(locationChoice){
            "gps" -> requestLocationPermission()
            "map" -> {
                // later
            }
        }
        if (notificationChoice == "enable"){
            requestNotificationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, Context.DEVICE_ID_DEFAULT)
        when(requestCode){
            REQUEST_LOCATION_PERMISSION ->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // Permission granted, proceed with GPS
                } else{
                    // Permission denied, show message
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_NOTIFICATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, enable notifications
                } else {
                    // Permission denied, disable notifications
                    with(sharedPreferences.edit()) {
                        putString("initial_notification_choice", "disable")
                        apply()
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return try {
            val navHostFragment = supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment
            navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()

        }catch (e: IllegalStateException){
            Log.e("MainActivity" , "NavigateUp: $e")
            super.onSupportNavigateUp()
        }
    }
    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
        private const val REQUEST_NOTIFICATION_PERMISSION = 101
    }
}