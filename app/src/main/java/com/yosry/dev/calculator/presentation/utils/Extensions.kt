package com.yosry.dev.calculator.presentation.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

fun getMessageDateCategory(timestamp: Long): String {
    val messageCalendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    val currentCalendar = Calendar.getInstance()

    // Check if Today
    if (messageCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
        messageCalendar.get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR)
    ) {
        return "Today"
    }

    // Check if Yesterday
    currentCalendar.add(Calendar.DAY_OF_YEAR, -1)
    if (messageCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
        messageCalendar.get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR)
    ) {
        return "Yesterday"
    }

    // Otherwise format as "Month DD, YYYY" (e.g., "May 24, 2026")
    val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}


// PermissionUtils.kt

@Composable
fun NotificationPermissionHandler() {
    val context = LocalContext.current
    var showExplanationDialog by remember { mutableStateOf(false) }

    // This is the launcher that triggers the actual Android system permission dialog
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        showExplanationDialog = false
        if (isGranted) {
            // Permission granted, workers will now be able to show notifications
        } else {
            // Permission denied. You can handle this gracefully if needed.
        }
    }

    // Check if we even need to ask (Android 13+ only)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionStatus = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        )

        // If not granted yet, show our custom explanation dialog first
        LaunchedEffect(permissionStatus) {
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                showExplanationDialog = true
            }
        }
    }

    // The Custom UI Dialog
    if (showExplanationDialog) {
        AlertDialog(
            onDismissRequest = {
                // Optional: set to false if you want them to be able to click outside to dismiss
                showExplanationDialog = false
            },
            title = {
                Text(text = "Allow Notifications", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Text(
                    text = "This app requires notification permissions to alert you when you lose internet connection or when your battery is critically low. Please grant this permission to ensure background alerts work properly."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // User clicked OK, now launch the real Android system prompt
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExplanationDialog = false }
                ) {
                    Text("Not Now")
                }
            }
        )
    }
}