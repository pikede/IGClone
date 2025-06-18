package com.example.instagram.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
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
import com.example.instagram.common.util.Constants.SINGLE_POST
import com.example.instagram.core_ui_components.CommonProgressSpinner
import com.example.instagram.core_ui_components.LikeAnimation
import com.example.instagram.core_ui_components.images.CommonAsyncImage
import com.example.instagram.core_ui_components.images.CommonImage
import com.example.instagram.core_ui_components.images.UserImageCard
import com.example.instagram.models.PostData
import com.example.instagram.ui.theme.InstagramTheme
import kotlinx.coroutines.delay

@Composable
fun FeedRoute(navController: NavController, modifier: Modifier = Modifier) {
    Feed(navController = navController, modifier = modifier)
}

@Composable
private fun Feed(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: IgViewModel = hiltViewModel(), // TODO create viewmodel for this
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    FeedContent(
        state = state,
        viewModel = viewModel,
        modifier = modifier,
        navController = navController
    )

    // todo whenever resume here get personalize feed
}

@Composable
private fun FeedContent(
    state: SignupScreenState,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: IgViewModel,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White)
        ) {
            UserImageCard(userImage = state.user?.imageUrl)
        }

        PostsList(
            posts = viewModel.userFeed.value,
            modifier = Modifier.weight(1f),
            isLoading = state.inProgress || viewModel.isFeedInProgress.value, // todo group this in the viewstate
            navController = navController,
            viewModel = viewModel,
            currentUserId = state.user?.userId.orEmpty()
        )

        BottomNavigationMenu(selectedItem = BottomNavigationItem.FEED, navController)
    }
}

@Composable
fun PostsList(
    posts: List<PostData>,
    isLoading: Boolean,
    navController: NavController,
    viewModel: IgViewModel,
    currentUserId: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        LazyColumn {
            items(items = posts, key = { it.postId.hashCode() }) {
                Post(
                    postData = it,
                    currentUserId = currentUserId,
                    viewModel = viewModel,
                    onPostClick = {
                        navigateTo(
                            navController = navController,
                            destination = DestinationScreen.SinglePost(it.postId),
                            NavParam(SINGLE_POST, it)
                        )
                    }
                )
            }
        }
        if (isLoading) {
            CommonProgressSpinner()
        }
    }
}

@Composable
fun Post(
    postData: PostData,
    currentUserId: String,
    viewModel: IgViewModel,
    onPostClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isLikeAnimation by remember { mutableStateOf(false) }
    var isDisLikeAnimation by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(corner = CornerSize(4.dp)),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = CircleShape, modifier = Modifier
                        .padding(4.dp)
                        .size(32.dp)
                ) {
                    CommonImage(data = postData.userImage, contentScale = ContentScale.Crop)
                }

                Text(text = postData.username.orEmpty(), modifier = Modifier.padding(4.dp))
            }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val postImageModifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (postData.likes?.contains(currentUserId) == true) {
                                    isDisLikeAnimation = true
                                } else {
                                    isLikeAnimation = true
                                }
                                viewModel.onLikePost(postData)
                            },
                            onTap = { onPostClick.invoke() }
                        )
                    }
                    .defaultMinSize(minHeight = 50.dp)
                CommonAsyncImage(
                    data = postData.postImage,
                    modifier = postImageModifier,
                    contentScale = ContentScale.FillWidth
                )

                // todo fix animation not showing
                HandeLikeDislikeAnimation(
                    isLikeAnimation = isLikeAnimation,
                    isDisLikeAnimation = isDisLikeAnimation,
                    onAnimationEnd = {
                        isDisLikeAnimation = false
                        isLikeAnimation = false
                    }
                )
            }
        }
    }
}

@Composable
private fun HandeLikeDislikeAnimation(
    isLikeAnimation: Boolean,
    isDisLikeAnimation: Boolean,
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(isLikeAnimation, isDisLikeAnimation) {
        delay(2000L)
        onAnimationEnd()
    }
    LikeAnimation(isLike = isLikeAnimation, modifier = modifier)
}

@Preview(showBackground = true)
@Composable
internal fun FeedPreview() = InstagramTheme {
    Feed(navController = rememberNavController())
} // todo add screenshot tests with papparazzi