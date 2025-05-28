package com.example.instagram.single_post

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
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
import com.example.instagram.core_ui_components.CommonDivider
import com.example.instagram.core_ui_components.CommonImage
import com.example.instagram.core_ui_components.ShowErrorModal
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
) { // todo fix sync issue from using not updating and using different viewModels
    val state by vm.state.collectAsStateWithLifecycle()

    val comments = vm.commentsState.value
    LaunchedEffect(Unit) {
        vm.getComments()
    }

    SinglePostScreen(
        state = state,
        navController = navController,
        modifier = modifier,
        commentsSize = comments.size
    )
}

@Composable
private fun SinglePostScreen(
    state: SinglePostViewState,
    navController: NavController,
    modifier: Modifier = Modifier,
    commentsSize: Int,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp)
    ) {
        Text(text = "Back", modifier = Modifier.clickable { navController.popBackStack() })

        CommonDivider()

        SinglePostDisplay(state, navController, commentsSize = commentsSize)
    }

    state.error?.let { error ->
        ShowErrorModal(
            error = error,
            onDismiss = { state.eventSink(SinglePostScreenEvent.ConsumeError) })
    }
}

// TODO split into image, likes descriptions, component
@OptIn(ExperimentalCoilApi::class)
@Composable
private fun SinglePostDisplay(
    state: SinglePostViewState,
    navController: NavController,
    commentsSize: Int,
    modifier: Modifier = Modifier,
) {
    val userData = state.user
    Box(
        modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Card(
                shape = CircleShape, modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp)
            ) {
                Image(
                    painter = rememberImagePainter(data = userData?.imageUrl),
                    contentDescription = null
                )
            }
            Text(text = state.postData?.username.orEmpty())
            Text(text = ".", modifier = Modifier.padding(8.dp))
            if (userData?.userId == state.postData?.userId) {
                // current userPost. Don't show anything
            } else if (userData?.following?.contains(state.postData?.userId) == true) {
                Text(
                    text = "Following",
                    color = Color.Gray,
                    modifier = Modifier.clickable {
                        state.postData?.userId?.let { state.onFollow(it) }
                    })
            } else {
                Text(
                    text = "Follow",
                    color = Color.Blue,
                    modifier = Modifier.clickable {
                        state.postData?.userId?.let { state.onFollow(it) }
                    })
            }
        }
    }
    Box {
        val modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 150.dp)
        CommonImage(
            data = state.postData?.postImage,
            modifier = modifier,
            contentScale = ContentScale.FillWidth
        )
    }
    Row(
        Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_like),
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
        Text(text = state.postData?.postDescription.orEmpty(), modifier = Modifier.padding(8.dp))
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

@Preview(showBackground = true)
@Composable
private fun SinglePostScreenPreview() = InstagramTheme {
    SinglePostScreen(
        state = SinglePostViewState.preview(),
        navController = rememberNavController(),
        commentsSize = 0
    )
}