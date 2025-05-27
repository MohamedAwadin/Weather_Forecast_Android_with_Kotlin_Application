package com.example.climo.alerts.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.climo.alerts.receiver.AlertReceiver

class AlertWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val alertId = inputData.getInt("alertId" , 0)
        val cityName = inputData.getString("cityName") ?: "Unknown"
        val fromDateTime = inputData.getString("fromDateTime") ?: ""
        val toDateTime = inputData.getString("toDateTime") ?: ""
        val alertType = inputData.getString("alertType") ?: "NOTIFICATION"

//        Log.d("AlertWorker", "Executing work for alertId: $alertId, city: $cityName, type: $alertType")

        Log.d("AlertWorker", "Starting work for alertId: $alertId, city: $cityName, type: $alertType")

        if (alertId == 0) {
            Log.e("AlertWorker", "Invalid alertId: $alertId")
            return Result.failure()
        }


         val intent = Intent(applicationContext , AlertReceiver::class.java).apply{
             action = "com.example.climo.ALERT"
             putExtra("alertId", alertId)
             putExtra("cityName", cityName)
             putExtra("fromDateTime", fromDateTime)
             putExtra("toDateTime", toDateTime)
             putExtra("alertType", alertType)
         }
        applicationContext.sendBroadcast(intent)
        Log.d("AlertWorker", "Broadcast sent for alertId: $alertId")

        return Result.success()
    }

}