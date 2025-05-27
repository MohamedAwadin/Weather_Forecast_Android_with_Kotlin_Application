package com.example.climo.alerts.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.climo.alerts.notification.NotificationHelper

class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
//        Log.d("AlertReceiver", "Received broadcast: ${intent.action}")
        Log.d("AlertReceiver", "Received broadcast: ${intent.action}, extras: ${intent.extras?.keySet()?.joinToString()}")
        val alertId = intent.getIntExtra("alertId", 0)
        val cityName = intent.getStringExtra("cityName") ?: "Unknown"
        val fromDateTime = intent.getStringExtra("fromDateTime") ?: ""
        val toDateTime = intent.getStringExtra("toDateTime") ?: ""
        val alertType = intent.getStringExtra("alertType") ?: "NOTIFICATION"

//        NotificationHelper.showNotification(
//            context,
//            alertId,
//            cityName,
//            "Weather alert from $fromDateTime to $toDateTime",
//            alertType == "HEADS_UP"
//        )


        when (intent.action) {
            "com.example.climo.ALERT" -> {
                Log.d("AlertReceiver", "Processing alert: id=$alertId, city=$cityName, type=$alertType")
                NotificationHelper.showNotification(
                    context,
                    alertId,
                    cityName,
                    "Weather alert from $fromDateTime to $toDateTime",
                    alertType == "HEADS_UP"
                )
            }
            "DISMISS" -> {
                Log.d("AlertReceiver", "Dismissed alert: id=$alertId")
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.cancel(alertId)
            }
            else -> Log.w("AlertReceiver", "Unknown action: ${intent.action}")
        }
    }
}