package com.example.instagram.my_posts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.instagram.core_ui_components.CommonProgressSpinner
import com.example.instagram.models.PostData

@Composable
fun PostList(
    isContextLoading: Boolean,
    postsLoading: Boolean,
    posts: List<PostData>,
    modifier: Modifier = Modifier,
    onPostClick: (PostData) -> Unit,
) { // todo split up the searchroute and simplify
    when {
        postsLoading -> CommonProgressSpinner()
        posts.isEmpty() -> MyPostsEmpty(isContextLoading, modifier) // todo should this be isContextLoading
        else -> MyPostsItems(posts = posts, onPostClick = onPostClick, modifier = modifier)
    }
}

@Composable
private fun MyPostsEmpty(isContextLoading: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isContextLoading) Text(text = "No posts available")
    }
}