package com.example.instagram

import androidx.navigation.NavController

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