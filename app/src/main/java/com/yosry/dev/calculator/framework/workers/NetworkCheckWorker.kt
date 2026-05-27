package com.yosry.dev.calculator.framework.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yosry.dev.calculator.R
import com.yosry.dev.calculator.framework.utils.isNetworkAvailable

class NetworkCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!isNetworkAvailable(context)) {
            showOfflineNotification()
        }
        return Result.success()
    }

    private fun showOfflineNotification() {
        val channelId = "offline_alerts"
        val notificationId = 1001
        val vibratePattern = longArrayOf(0, 500, 200, 500)
        // 1. Create Notification Channel (Required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Connectivity Alerts"
            val descriptionText = "Alerts when the device loses internet connection"
            // IMPORTANCE_HIGH ensures it makes a sound and pops up on screen
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = vibratePattern
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Build the Notification
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)


        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_settings) // Replace with your app's small icon
            .setContentTitle("Connection Lost")
            .setContentText("You are currently offline. Please connect to the internet.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(defaultSoundUri)
            .setVibrate(vibratePattern) // Add vibration here
            .setAutoCancel(true)
        // Notice there is NO setContentIntent() here, so clicking it does nothing

        // 3. Show the notification (checking permission for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            }
        } else {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }
}