package com.yosry.dev.calculator.framework.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.os.BatteryManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.content.pm.PackageManager
import com.yosry.dev.calculator.R

class BatteryCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val prefs = context.getSharedPreferences("battery_prefs", Context.MODE_PRIVATE)

    override suspend fun doWork(): Result {
        // 1. Get Battery Status
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }

        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = (level * 100) / scale

        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        // 2. Logic: Reset if charging
        if (isCharging) {
            // User plugged in the phone, reset our tracking
            prefs.edit().putInt("last_notified_threshold", 100).apply()

            // Auto-cancel the extreme 10% notification if they plugged it in
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1002)
            return Result.success()
        }

        // 3. Logic: Check thresholds if below 50%
        if (batteryPct <= 50) {
            val lastThreshold = prefs.getInt("last_notified_threshold", 100)

            // Round down to nearest 5% (e.g. 48% becomes 45, 52% becomes 50)
            val currentThresholdBucket = (batteryPct / 5) * 5

            // Only notify if we have dropped into a NEW 5% bucket
            if (currentThresholdBucket < lastThreshold && currentThresholdBucket <= 50) {

                val isCritical = batteryPct <= 10
                showBatteryNotification(batteryPct, isCritical)

                // Save this threshold so we don't spam the user until it drops another 5%
                prefs.edit().putInt("last_notified_threshold", currentThresholdBucket).apply()
            }
        }

        return Result.success()
    }

    private fun showBatteryNotification(batteryPct: Int, isCritical: Boolean) {
        val channelId = "battery_alerts"
        val notificationId = 1002

        // Setup Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Battery Alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = "Alerts for low battery thresholds"
                enableVibration(true)
                // Stronger vibration pattern for 10%
                vibrationPattern = if (isCritical) longArrayOf(0, 1000, 500, 1000, 500, 1000) else longArrayOf(0, 500, 200, 500)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Customize text based on severity
        val title = if (isCritical) "CRITICAL BATTERY" else "Battery Low ($batteryPct%)"
        val message = if (isCritical) {
            "Battery is at $batteryPct%. Phone will shut down soon. Charge immediately!"
        } else {
            "You have to charge your phone now. Battery has dropped to $batteryPct%."
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_settings) // Replace with your battery icon if you have one
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(if (isCritical) longArrayOf(0, 1000, 500, 1000, 500, 1000) else longArrayOf(0, 500, 200, 500))

            // Set AutoCancel. If true, tapping it removes it. If false, it sticks until swiped.
            .setAutoCancel(!isCritical)

            // If critical (10%), make it an "Ongoing" notification so it CANNOT be swiped away easily
            .setOngoing(isCritical)

        // Show Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            }
        } else {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }
}