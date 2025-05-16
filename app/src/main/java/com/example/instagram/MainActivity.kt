package com.example.instagram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.instagram.auth.login.LoginRoute
import com.example.instagram.auth.signup.SignupRoute
import com.example.instagram.comments.CommentsRoute
import com.example.instagram.common.util.Constants.POST_ID
import com.example.instagram.feed.FeedRoute
import com.example.instagram.feed.SearchRoute
import com.example.instagram.my_posts.MyPostsRoute
import com.example.instagram.new_post.NewPostRoute
import com.example.instagram.profile.ProfileRoute
import com.example.instagram.single_post.SinglePostRoute
import com.example.instagram.ui.theme.InstagramTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InstagramTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    InstagramApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

sealed interface DestinationScreen {
    @Serializable
    object Signup : DestinationScreen

    @Serializable
    object Login : DestinationScreen

    @Serializable
    object Feed : DestinationScreen

    @Serializable
    object Search : DestinationScreen

    @Serializable
    object MyPosts : DestinationScreen

    @Serializable
    object Profile : DestinationScreen

    @Serializable
    data class NewPost(val imageUri: String) : DestinationScreen

    @Serializable
    data class SinglePost(val postId: String?) : DestinationScreen

    @Serializable
    data class Comments(val postId: String) : DestinationScreen
}

@Composable
fun InstagramApp(
    modifier: Modifier = Modifier,
    viewModel: IgViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = DestinationScreen.Signup) {
        composable<DestinationScreen.Signup> {
            SignupRoute(navController = navController, modifier = modifier)
        }
        composable<DestinationScreen.Login> {
            LoginRoute(navController = navController, modifier = modifier)
        }
        composable<DestinationScreen.Feed> {
            FeedRoute(navController = navController, modifier = modifier)
        }
        composable<DestinationScreen.Search> {
            SearchRoute(navController = navController, modifier = modifier)
        }
        composable<DestinationScreen.MyPosts> {
            MyPostsRoute(navController = navController, modifier = modifier)
        }
        composable<DestinationScreen.Profile> {
            ProfileRoute(navController = navController, modifier = modifier)
        }
        composable<DestinationScreen.NewPost> { navBackstackEntry ->
            val imageUri = navBackstackEntry.toRoute<DestinationScreen.NewPost>().imageUri
            NewPostRoute(navController = navController, encodedUri = imageUri, modifier = modifier)
        }
        composable<DestinationScreen.SinglePost> { navBackStackEntry ->
            SinglePostRoute(
                navController = navController,
                modifier = modifier
            )
        }
        composable<DestinationScreen.Comments> { navBackstackEntry ->
            val postId = navBackstackEntry.arguments?.getString(POST_ID)
            postId?.let {
                CommentsRoute(
                    postId = it,
                    modifier = modifier
                )
            }
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