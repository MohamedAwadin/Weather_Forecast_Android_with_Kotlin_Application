package com.example.climo.alerts.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Message
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.climo.R
import com.example.climo.alerts.receiver.AlertReceiver

object NotificationHelper {

    private const val CHANNEL_ID = "weather_alerts"
    private const val CHANNEL_NAME = "Weather Alerts"


    fun showNotification(context: Context, alertId: Int, cityName: String , message: String , isHeadUp: Boolean){

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Android 8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                if (isHeadUp){
                    NotificationManager.IMPORTANCE_HIGH
                } else {
                    NotificationManager.IMPORTANCE_DEFAULT
                }
            ).apply {
                if (isHeadUp){
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }

        val dismissIntent = Intent(context , AlertReceiver::class.java).apply {
            action = "DISMISS"
            putExtra("alertId", alertId)
        }

        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            alertId,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context , CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("Weather Alert for $cityName")
            .setContentText(message)
            .setPriority(if (isHeadUp) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setSound(if (isHeadUp) RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) else null)
            .addAction(R.drawable.ic_cancel, "Dismiss" , dismissPendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(alertId , notification)
        Log.d("NotificationHelper", "Notification sent: id=$alertId, city=$cityName, headsUp=$isHeadUp")
    }



}