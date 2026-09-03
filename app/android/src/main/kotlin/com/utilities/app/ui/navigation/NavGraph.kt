package com.utilities.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.utilities.app.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onUtilityClick = { route ->
                    navController.navigate(route)
                }
            )
        }
        
        composable("filetransfer") {
            FileTransferScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("joystick") {
            JoystickScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("speaker") {
            SpeakerScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("steering") {
            SteeringScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("mic") {
            MicScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
