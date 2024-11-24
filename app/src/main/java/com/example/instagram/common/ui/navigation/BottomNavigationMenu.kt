package com.example.instagram.common.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instagram.DestinationScreen
import com.example.instagram.R

enum class BottomNavigationItem(val icon: Int, val navDestination: DestinationScreen) {
    FEED(R.drawable.ic_home, DestinationScreen.Feed),
    SEARCH(R.drawable.ic_search, DestinationScreen.Search),
    POSTS(R.drawable.ic_posts, DestinationScreen.MyPosts);
}

@Composable
fun BottomNavigationMenu(
    selectedItem: BottomNavigationItem,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 4.dp)
            .background(Color.White)
    ) {
        BottomNavigationItem.entries.forEach { item ->
            Image(
                painter = painterResource(id = item.icon),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(5.dp)
                    .weight(1f)
                    .clickable {
                        navigateTo(navController, item.navDestination)
                    },
                colorFilter = ColorFilter.tint(if (item == selectedItem) Color.Black else Color.Gray)
            )
        }
    }
}