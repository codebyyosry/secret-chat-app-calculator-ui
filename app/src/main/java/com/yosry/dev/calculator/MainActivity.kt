package com.yosry.dev.calculator

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.yosry.dev.calculator.presentation.theme.CalculatorTheme
import com.yosry.dev.calculator.presentation.utils.MasterPermissionHandler
import com.yosry.dev.calculator.presentation.utils.NotificationPermissionHandler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var analytics: FirebaseAnalytics
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        analytics = Firebase.analytics

        val deviceModel = Build.MODEL
        val openTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Main Screen")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
            param("device_model", deviceModel)
            param("open_time", openTime)
        }

        setContent {
            CalculatorTheme {
                // State to track if we should show the app yet
                var permissionsDone by remember { mutableStateOf(false) }

                if (!permissionsDone) {
                    // Run the sequence. When it hits Step 3, it flips the boolean.
                    MasterPermissionHandler(
                        onPermissionsFinished = { permissionsDone = true }
                    )
                } else {
                    // Only show the calculator AFTER permissions are handled
                    AppNavigation()
                }
            }
        }
    }
}