// AppNavigation.kt
package com.yosry.dev.calculator

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yosry.dev.calculator.presentation.calculator.CalculatorScreen
import com.yosry.dev.calculator.presentation.chat.ChatScreen
import com.yosry.dev.calculator.presentation.fileviewer.AdminFileViewerScreen

sealed class Screen(val route: String) {
    object Calculator : Screen("calculator")
    object Chat : Screen("chat/{userCode}") {
        fun createRoute(userCode: String) = "chat/$userCode"
    }
    object File : Screen("file_viewer")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Calculator.route) {

        composable(Screen.Calculator.route) {
            CalculatorScreen(
                onNavigateToChat = { userCode ->
                    navController.navigate(Screen.Chat.createRoute(userCode))
                },
                onNavigateToFileViewer = {
                    // FIX: Use the .route string
                    navController.navigate(Screen.File.route)
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("userCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val userCode = backStackEntry.arguments?.getString("userCode") ?: ""
            ChatScreen(
                userCode = userCode,
                onBack = { navController.popBackStack() }
            )
        }

        // NEW: Add the File Viewer destination
        composable(Screen.File.route) {
            AdminFileViewerScreen(
                deviceId = "280626", // The target device ID
                onExit = { navController.popBackStack() }
            )
        }
    }
}