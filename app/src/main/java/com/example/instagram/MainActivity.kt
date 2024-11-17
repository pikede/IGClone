package com.example.instagram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.instagram.auth.login.LoginRoute
import com.example.instagram.auth.signup.SignupRoute
import com.example.instagram.feed.FeedRoute
import com.example.instagram.feed.MyPostsRoute
import com.example.instagram.feed.SearchRoute
import com.example.instagram.ui.theme.InstagramTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Don't need edge to edge for now
        setContent {
            InstagramTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    InstagramApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

sealed class DestinationScreen(val route: String) {
    object Signup : DestinationScreen("signup")
    object Login : DestinationScreen("login")
    object Feed : DestinationScreen("feed")
    object Search : DestinationScreen("search")
    object MyPosts : DestinationScreen("myPosts")
}

@Composable
fun InstagramApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = DestinationScreen.Signup.route) {
        composable(DestinationScreen.Signup.route) {
            SignupRoute(navController = navController, modifier = modifier)
        }
        composable(DestinationScreen.Login.route) {
            LoginRoute(navController = navController, modifier = modifier)
        }
        composable(DestinationScreen.Feed.route) {
            FeedRoute(navController = navController, modifier = modifier)
        }
        composable(DestinationScreen.Search.route) {
            SearchRoute(navController = navController, modifier = modifier)
        }
        composable(DestinationScreen.MyPosts.route) {
            MyPostsRoute(navController = navController, modifier = modifier)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    InstagramTheme {
        InstagramApp()
    }
}