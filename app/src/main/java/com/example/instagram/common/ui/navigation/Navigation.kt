package com.example.instagram.common.ui.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.example.instagram.DestinationScreen

data class NavParam(val name: String, val value: Parcelable)

// todo move to core-ui or common-ui module
fun navigateTo(
    navController: NavController,
    destination: DestinationScreen,
    vararg params: NavParam,
) {
    for (param in params) {
        navController.currentBackStackEntry?.arguments?.putParcelable(param.name, param.value)
    }

    navController.navigate(destination) {
        popUpTo(destination)
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
        navController.navigate(DestinationScreen.Feed) {
            popUpTo(0) // removes all composable when navigating to feed
        }
    }
}

fun logOutAndClearBackstack(navController: NavController) {
    navController.navigate(DestinationScreen.Login) {
        popUpTo(0)
    }
}