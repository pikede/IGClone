package com.example.instagram.feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.instagram.core_ui.navigation.BottomNavigationItem
import com.example.instagram.core_ui.navigation.BottomNavigationMenu
import com.example.instagram.ui.theme.InstagramTheme

@Composable
fun SearchRoute(navController: NavController, modifier: Modifier = Modifier) {
    SearchScreen(navController = navController, modifier = modifier)
}

@Composable
private fun SearchScreen(navController: NavController, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Search screen")
        }
        BottomNavigationMenu(selectedItem = BottomNavigationItem.SEARCH, navController)
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() = InstagramTheme {
    SearchScreen(navController = rememberNavController())
}