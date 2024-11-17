package com.example.instagram.core_ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

@Composable
fun CheckSignedIn(
    signedIn: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier,
) { // TODO rename to NavigateToFeedIfSignedIn
    var alreadyLoggedIn by rememberSaveable { mutableStateOf(false) }

    if (signedIn && !alreadyLoggedIn) {
        alreadyLoggedIn = true
        navController.navigate(DestinationScreen.Feed.route) {
            popUpTo(0) // removes all composable when navigating to feed
        }
    }
}