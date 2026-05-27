package com.yosry.dev.calculator.presentation.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun MasterPermissionHandler(onPermissionsFinished: () -> Unit) {
    val context = LocalContext.current

    // Tracks which permission we are currently asking for
    var permissionStep by remember { mutableIntStateOf(1) }

    // Launcher 1: For the Notification Popup
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Regardless of if they said yes or no, move to the next step
        permissionStep = 2
    }

    // Launcher 2: For the Settings Screen (All Files Access)
    val storageSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // They returned from the settings screen, we are done!
        permissionStep = 3
    }

    // The Sequence Logic
    LaunchedEffect(permissionStep) {
        when (permissionStep) {
            1 -> {
                // Step 1: Check and request Notifications (Android 13+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    // Already granted or older Android version, skip to step 2
                    permissionStep = 2
                }
            }
//            2 -> {
//                // Step 2: Check and request All Files Access (Android 11+)
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
//                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
//                        data = Uri.parse("package:${context.packageName}")
//                    }
//                    // We use the launcher here so we know exactly when they return to the app
//                    storageSettingsLauncher.launch(intent)
//                } else {
//                    // Already granted or older Android version, skip to step 3
//                    permissionStep = 3
//                }
//            }
            2 -> {
                // Step 3: Everything is handled, let the app load
                onPermissionsFinished()
            }
        }
    }
}