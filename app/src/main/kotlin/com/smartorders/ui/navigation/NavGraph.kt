package com.smartorders.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartorders.ui.screens.dashboard.DashboardScreen
import com.smartorders.ui.screens.login.LoginScreen

sealed class Screen(val route: String) {
    object Login     : Screen("login")
    object Dashboard : Screen("dashboard")
}

@Composable
fun SmartOrdersNavGraph(
    navController: NavHostController = rememberNavController()
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        NavHost(navController = navController, startDestination = Screen.Login.route) {

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
