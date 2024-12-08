package com.example.instagram.feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.instagram.common.ui.navigation.BottomNavigationItem
import com.example.instagram.common.ui.navigation.BottomNavigationMenu
import com.example.instagram.ui.theme.InstagramTheme

@Composable
fun FeedRoute(navController: NavController, modifier: Modifier = Modifier) {
    Feed(navController = navController, modifier = modifier)
}

@Composable
private fun Feed(
    navController: NavController,
    modifier: Modifier = Modifier,
//    vm: MyPostsViewModel = hiltViewModel<MyPostsViewModel>(), TODO create viewmodel for this
) {
//    val state by vm.state.collectAsStateWithLifecycle() TODO observe state from the viewmodel
    FeedContent(
        navController = navController,
        modifier = modifier
    )
}

@Composable
private fun FeedContent(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Feed screen")
        }
        BottomNavigationMenu(selectedItem = BottomNavigationItem.FEED, navController)
    }
}

@Preview(showBackground = true)
@Composable
internal fun FeedPreview() = InstagramTheme {
    Feed(navController = rememberNavController())
} // todo add screenshot tests with papparazzi