package com.example.climo.alerts.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.climo.alerts.notification.NotificationHelper
import com.example.climo.data.local.ClimoDatabase
import com.example.climo.data.model.WeatherAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        Log.d("AlertReceiver", "Received broadcast: ${intent.action}, extras: ${intent.extras?.keySet()?.joinToString()}")
        val alertId = intent.getIntExtra("alertId", 0)
        val cityName = intent.getStringExtra("cityName") ?: "Unknown"
        val fromDateTime = intent.getStringExtra("fromDateTime") ?: ""
        val toDateTime = intent.getStringExtra("toDateTime") ?: ""
        val alertType = intent.getStringExtra("alertType") ?: "NOTIFICATION"
        val weatherDescription = intent.getStringExtra("weatherDescription") ?: "Weather data unavailable"

        when (intent.action) {
            "com.example.climo.ALERT" -> {
                Log.d("AlertReceiver", "Processing alert: id=$alertId, city=$cityName, type=$alertType")
                NotificationHelper.showNotification(
                    context,
                    alertId,
                    cityName,
                    "Weather: $weatherDescription from $fromDateTime to $toDateTime",
                    alertType == "HEADS_UP"
                )
            }
            "DISMISS" -> {
                Log.d("AlertReceiver", "Dismissed alert: id=$alertId")
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.cancel(alertId)
                NotificationHelper.stopSound()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val dao = ClimoDatabase.getDatabase(context).weatherAlertDao()
                        val alert = WeatherAlert(
                            id = alertId,
                            cityName=cityName,
                            latitude = 0.0,
                            longitude = 0.0,
                            fromDateTime = fromDateTime,
                            toDateTime = toDateTime,
                            alertType = alertType
                        )
                        dao.delete(alert)
                        Log.d("AlertReceiver", "Deleted alert from database: id=$alertId")

                    }catch (e: Exception){
                        Log.e("AlertReceiver", "Error deleting alert: ${e.message}")
                    }
                }
            }
            else -> Log.w("AlertReceiver", "Unknown action: ${intent.action}")
        }
    }
}