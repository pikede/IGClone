package com.example.instagram.feed

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.instagram.IgViewModel
import com.example.instagram.auth.signup.SignupScreenState
import com.example.instagram.common.ui.navigation.BottomNavigationItem
import com.example.instagram.common.ui.navigation.BottomNavigationMenu
import com.example.instagram.common.ui.navigation.NavParam
import com.example.instagram.common.ui.navigation.navigateTo
import com.example.instagram.common.util.Constants.POSTS
import com.example.instagram.core_ui_components.BlackTransparentTextContainer
import com.example.instagram.my_posts.PostList
import com.example.instagram.ui.theme.InstagramTheme

// TODO add search viewmodel currently using [@IgViewModel]
@Composable
fun SearchRoute(navController: NavController, modifier: Modifier = Modifier) {
    SearchScreen(navController = navController, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(
    navController: NavController,
    viewModel: IgViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchScreen(modifier, viewModel, state, navController)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SearchScreen(
    modifier: Modifier,
    vm: IgViewModel,
    state: SignupScreenState,
    navController: NavController,
) {
    var searchTerm by rememberSaveable { mutableStateOf("") }

    Column(modifier) {
        SearchBar(
            searchTerm = searchTerm,
            onSearchChange = { searchTerm = it },
            onSearch = { vm.searchPosts(searchTerm) })

        PostList(
            isContextLoading = false,
            postsLoading = state.searchedPostsProgress,
            posts = state.searchedPosts,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
        ) { post ->
            navigateTo(
                navController, destination = DestinationScreen.SinglePost,
                NavParam(POSTS, post)
            )
        }

        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.SEARCH,
            navController = navController
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchTerm: String,
    onSearchChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    // todo remove the line under the textfield
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