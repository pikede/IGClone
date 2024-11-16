package com.example.instagram.core_ui

import androidx.navigation.NavController
import com.example.instagram.DestinationScreen

// todo move to core-ui module
fun navigateTo(
    navController: NavController,
    destination: DestinationScreen,
) {
    navController.navigate(destination.route) {
        popUpTo(destination.route)
        launchSingleTop = true
    }
}