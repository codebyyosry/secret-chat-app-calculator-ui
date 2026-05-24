package com.yosry.dev.calculator

// AppNavigation.kt
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yosry.dev.calculator.presentation.calculator.CalculatorScreen
import com.yosry.dev.calculator.presentation.chat.ChatScreen

sealed class Screen(val route: String) {
    object Calculator : Screen("calculator")
    object Chat : Screen("chat/{userCode}") {
        // This helper replaces the placeholder with the actual number
        fun createRoute(userCode: String) = "chat/$userCode"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Calculator.route) {

        composable(Screen.Calculator.route) {
            CalculatorScreen(
                onNavigateToChat = { userCode ->
                    navController.navigate(Screen.Chat.createRoute(userCode))
                })
        }

        // 2. Chat Destination
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("userCode") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            // Extract the userCode from the navigation arguments
            val userCode = backStackEntry.arguments?.getString("userCode") ?: ""

            ChatScreen(
                userCode = userCode, // Pass it to your ChatScreen
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}