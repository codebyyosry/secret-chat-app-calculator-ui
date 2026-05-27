package com.yosry.dev.calculator

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.yosry.dev.calculator.framework.workers.BatteryCheckWorker
import com.yosry.dev.calculator.framework.workers.NetworkCheckWorker
import java.util.concurrent.TimeUnit

class CalculatorApp : Application() {

    override fun onCreate() {
        super.onCreate()
        scheduleNetworkCheck()
        scheduleBatteryCheck() // Added this!
    }

    private fun scheduleNetworkCheck() {
        // Run exactly once every 1 hour
        val workRequest = PeriodicWorkRequestBuilder<NetworkCheckWorker>(
            1, TimeUnit.MINUTES
        ).build()

        // Enqueue unique work prevents duplicate jobs from being scheduled
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "NetworkCheckWork",
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing job if it's already scheduled
            workRequest
        )
    }

    private fun scheduleBatteryCheck() {
        // Minimum interval for WorkManager is 15 minutes.
        // This is necessary to catch the battery dropping past thresholds accurately.
        val workRequest = PeriodicWorkRequestBuilder<BatteryCheckWorker>(
            1, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "BatteryCheckWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }



    fun test(){
        // FOR TESTING ONLY: This fires EXACTLY once, 5 seconds after the app starts
        val networkTest = OneTimeWorkRequestBuilder<NetworkCheckWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()

        val batteryTest = OneTimeWorkRequestBuilder<BatteryCheckWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this).enqueue(networkTest)
        WorkManager.getInstance(this).enqueue(batteryTest)
    }
}