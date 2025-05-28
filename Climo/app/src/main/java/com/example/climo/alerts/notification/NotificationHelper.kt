package com.example.climo.alerts.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.climo.R
import com.example.climo.alerts.receiver.AlertReceiver
import android.util.Log

object NotificationHelper {

    private const val CHANNEL_ID = "weather_alerts"
    private const val CHANNEL_NAME = "Weather Alerts"
    private var mediaPlayer: MediaPlayer? = null

    fun showNotification(context: Context, alertId: Int, cityName: String, message: String, isHeadsUp: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                if (isHeadsUp) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                if (isHeadsUp) {
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    setSound(Uri.parse("android.resource://${context.packageName}/raw/alert_long"), audioAttributes)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Dismiss intent
        val dismissIntent = Intent(context, AlertReceiver::class.java).apply {
            action = "DISMISS"
            putExtra("alertId", alertId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            alertId,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alert)
            .setContentTitle("Weather Alert for $cityName")
            .setContentText(message)
            .setPriority(if (isHeadsUp) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.ic_cancel, "Dismiss", dismissPendingIntent)
            .setAutoCancel(false) // Prevent swipe dismissal

        if (isHeadsUp) {
            notificationBuilder
                .setOngoing(true) // Make notification persistent
                .setSound(null) // Disable default sound; we'll handle it manually
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            // Start looping custom sound
            startLoopingSound(context)
        } else {
            notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }

        val notification = notificationBuilder.build()
        notificationManager.notify(alertId, notification)
        Log.d("NotificationHelper", "Notification sent: id=$alertId, city=$cityName, headsUp=$isHeadsUp")
    }

    private fun startLoopingSound(context: Context) {
        stopSound() // Ensure no existing sound is playing
        try {
            mediaPlayer = MediaPlayer.create(context, Uri.parse("android.resource://${context.packageName}/raw/alert_long")).apply {
                isLooping = true
                start()
            }
            Log.d("NotificationHelper", "Started looping sound")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error starting sound: ${e.message}")
            // Fallback to default alarm sound
            mediaPlayer = MediaPlayer.create(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)).apply {
                isLooping = true
                start()
            }
        }
    }

    fun stopSound() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
            Log.d("NotificationHelper", "Stopped looping sound")
        }
    }
}