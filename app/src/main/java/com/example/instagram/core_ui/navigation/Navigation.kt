package com.example.instagram.core_ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.example.instagram.DestinationScreen

// todo move to core-ui or common-ui module
fun navigateTo(
    navController: NavController,
    destination: DestinationScreen,
) {
    navController.navigate(destination.route) {
        popUpTo(destination.route)
        launchSingleTop = true
    }
}

@Composable
fun CheckSignedIn(
    signedIn: Boolean,
    navController: NavController,
) {
    var alreadyLoggedIn by rememberSaveable { mutableStateOf(false) }

    if (signedIn && !alreadyLoggedIn) {
        alreadyLoggedIn = true
        navController.navigate(DestinationScreen.MyPosts.route) {
            popUpTo(0) // removes all composable when navigating to feed
        }
    }
}