package com.example.instagram.search_post

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.instagram.DestinationScreen
import com.example.instagram.common.ui.navigation.BottomNavigationItem
import com.example.instagram.common.ui.navigation.BottomNavigationMenu
import com.example.instagram.common.ui.navigation.navigateTo
import com.example.instagram.core_ui_components.BlackTransparentTextContainer
import com.example.instagram.my_posts.components.PostList
import com.example.instagram.ui.theme.InstagramTheme

@Composable
fun SearchRoute(navController: NavController, modifier: Modifier = Modifier) {
    SearchScreen(navController = navController, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SearchPostsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchScreen(state = state, navController = navController, modifier = modifier)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SearchScreen(
    state: SearchPostState,
    navController: NavController,
    modifier: Modifier,
) {
    Scaffold(
        topBar = {
            SearchPostTopBar(
                searchTerm = state.searchTerm,
                onSearchChange = { state.onUpdateSearchTerm(it) },
                onSearch = { state.onSearch() })
        },
        bottomBar = {
            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.SEARCH,
                navController = navController,
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) { // todo qhy does this have to be a row or column for this to work?
                // todo add search postlist
            PostList(
                isContextLoading = false,
                postsLoading = state.inProgress,
                posts = state.searchedPosts,
                modifier = Modifier.fillMaxSize(),
            ) { post ->
                navigateTo(
                    navController = navController,
                    destination = DestinationScreen.SinglePost(post.postId)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPostTopBar(
    searchTerm: String,
    onSearchChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = searchTerm,
        onValueChange = onSearchChange,
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, CircleShape),
        shape = CircleShape,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
                focusManager.clearFocus()
            }
        ),
        maxLines = 1,
        singleLine = true,
        colors = BlackTransparentTextContainer(
            backgroundColor = Color.Transparent,
            textColor = Color.Black,
            focusedLabelColor = Color.Transparent,
            unfocusedLabelColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        trailingIcon = {
            IconButton(onClick = {
                onSearch()
                focusManager.clearFocus()
            }) {
                Icon(imageVector = Icons.Filled.Search, contentDescription = null)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() = InstagramTheme {
    SearchScreen(navController = rememberNavController())
}