package com.example.instagram.single_post

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.instagram.DestinationScreen
import com.example.instagram.R
import com.example.instagram.core_ui_components.ShowErrorModal
import com.example.instagram.core_ui_components.video.VideoPlayer
import com.example.instagram.single_post.components.SinglePostImage
import com.example.instagram.ui.theme.InstagramTheme

@Composable
fun SinglePostRoute(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    SinglePost(
        navController = navController,
        modifier = modifier
    )
}

@Composable
private fun SinglePost(
    navController: NavController,
    modifier: Modifier = Modifier,
    vm: SinglePostViewModel = hiltViewModel<SinglePostViewModel>(),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    val comments = vm.commentsState.value
    LaunchedEffect(Unit) {
        vm.getComments()
    }

    SinglePost(
        state = state,
        navController = navController,
        modifier = modifier,
        commentsSize = comments.size
    )
}

@Composable
private fun SinglePost(
    state: SinglePostViewState,
    navController: NavController,
    modifier: Modifier = Modifier,
    commentsSize: Int,
) {
    Scaffold(
        topBar = {
            Text(
                text = "Back",
                modifier = Modifier.clickable { navController.popBackStack() })
        },
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp)
    ) { paddingValues ->
        SinglePostContent(
            state = state,
            navController = navController,
            commentsSize = commentsSize,
            modifier = modifier
                .padding(paddingValues)
        )
    }
    state.error?.let { error ->
        ShowErrorModal(
            error = error,
            onDismiss = { state.eventSink(SinglePostScreenEvent.ConsumeError) })
    }
}

// TODO split into image, likes descriptions, and move to component
@OptIn(ExperimentalCoilApi::class)
@Composable
private fun SinglePostContent(
    state: SinglePostViewState,
    navController: NavController,
    commentsSize: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                ) {
                    Image(
                        painter = rememberImagePainter(data = state.postData?.userImage),
                        contentDescription = null
                    )
                }
                Text(text = state.postData?.username.orEmpty())
                if (state.isFollowingTextVisible) {
                    Text(
                        text = state.followText,
                        color = if (state.isFollowing) Color.Gray else Color.Blue,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable {
                                state.postData?.userId?.let { state.onFollow(it) }
                            }
                    )
                }
            }
        }

        // todo convert to type check in postdata object
        val post = state.postData
        if (post != null) {
            when {
                !post.postImage.isNullOrEmpty() -> SinglePostImage(postImage = post.postImage)
                !post.postVideo.isNullOrEmpty() -> VideoPlayer(post.postVideo)
                else -> Text(text = "Error no video or image to show in this post")
            }
        }

        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = if (state.postData?.likes?.isEmpty() == true) R.drawable.ic_dislike else R.drawable.ic_like),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(Color.Red)
            )
            Text(
                text = " ${state.postData?.likes?.size ?: 0} likes",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Row(modifier = Modifier.padding(8.dp)) {
            Text(text = state.postData?.username.orEmpty(), fontWeight = FontWeight.Bold)
            Text(
                text = state.postData?.postDescription.orEmpty(),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Row(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "Comments $commentsSize",
                color = Color.Gray,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable {
                        state.postData?.postId?.let {
                            navController.navigate(DestinationScreen.Comments(it))
                        }
                    })
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SinglePostScreenPreview() = InstagramTheme {
    SinglePost(
        state = SinglePostViewState.preview(),
        navController = rememberNavController(),
        commentsSize = 0
    )
}