package com.example.instagram.my_posts

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.instagram.DestinationScreen
import com.example.instagram.R
import com.example.instagram.common.ui.navigation.BottomNavigationItem
import com.example.instagram.common.ui.navigation.BottomNavigationMenu
import com.example.instagram.common.ui.navigation.navigateTo
import com.example.instagram.common.util.Constants.IMAGE_DIRECTORY
import com.example.instagram.common.util.Constants.VIDEO_DIRECTORY
import com.example.instagram.core_ui_components.CommonProgressSpinner
import com.example.instagram.core_ui_components.image.UserImageCard
import com.example.instagram.my_posts.components.PostList
import com.example.instagram.ui.theme.InstagramTheme

// todo rename package and files to profile
@Composable
fun MyPostsRoute(navController: NavController, modifier: Modifier = Modifier) {
    MyPosts(navController = navController, modifier = modifier)
}

@Composable
private fun MyPosts(
    navController: NavController,
    modifier: Modifier = Modifier,
    vm: MyPostsViewModel = hiltViewModel<MyPostsViewModel>(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val followers = vm.followers.intValue

    MyPosts(
        state = state,
        navController = navController,
        followers = followers,
        modifier = modifier,
    )

    LifecycleResumeEffect(Unit) {
//        vm.onRefresh() todo only refresh when the add is succesful
        onPauseOrDispose { vm.onRefresh() }//nothing to cleanup
    }
}

@Composable
private fun MyPosts(
    state: MyPostsViewState,
    navController: NavController,
    followers: Int,
    modifier: Modifier = Modifier,
) {
    // https://developer.android.com/training/data-storage/shared/photo-picker#compose
    val newPostImageLauncher =
        rememberLauncherForActivityResult(contract = PickVisualMedia()) { uri ->
            if (uri == null || uri.path?.isEmpty()== true) {
                return@rememberLauncherForActivityResult
            }
            val uriItem = uri.path!!
            val route = when {
                uriItem.contains(IMAGE_DIRECTORY) -> DestinationScreen.NewImagePost(imageUri = uriItem)
                uriItem.contains(VIDEO_DIRECTORY) -> DestinationScreen.NewVideoPost(uriItem)
                else -> return@rememberLauncherForActivityResult
            }
            navController.navigate(route)
        }
    Column(modifier) {
        Column(modifier = Modifier.weight(1f)) {
            MyPostHeader(state, newPostImageLauncher, followers)
            MyPostsAccountInformation(state)
            MyPostsEditProfile(navController)
            PostList(
                isContextLoading = state.inProgress,
                postsLoading = state.refreshPostsProgress,
                posts = state.posts,
                modifier = Modifier
                    .weight(1f)
                    .padding(1.dp)
                    .fillMaxSize(),
                onPostClick = { post ->
                    navigateTo(
                        navController,
                        DestinationScreen.SinglePost(post.postId)
                    )
                }
            )
        }
        BottomNavigationMenu(selectedItem = BottomNavigationItem.POSTS, navController)
    }

    if (state.inProgress) {
        CommonProgressSpinner()
    }
}

@Composable
private fun MyPostsAccountInformation(state: MyPostsViewState) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = state.user?.name.orEmpty(), fontWeight = FontWeight.Bold)
        Text(text = state.getUserName())
        Text(text = state.user?.bio.orEmpty())
    }
}

@Composable
private fun MyPostsEditProfile(navController: NavController) {
    OutlinedButton(
        onClick = { navigateTo(navController, DestinationScreen.Profile) },
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        shape = RoundedCornerShape(10)
    ) {
        Text(text = "Edit Profile", color = Color.Black)
    }
}

@Composable
private fun MyPostHeader(
    state: MyPostsViewState,
    newPostImageLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    followers: Int,
) {
    Row {
        ProfileImage(imageUrl = state.user?.imageUrl, onClick = {
            newPostImageLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageAndVideo)) // checks for any type of image and video on the device
        })
        Text(
            text = "${state.posts.size}\nPosts",
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            textAlign = TextAlign.Center
        )
        Text(
            text = "$followers\nFollowers",
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            textAlign = TextAlign.Center
        )
        Text(
            text = "${state.user?.following?.size ?: 0}\nFollowing",
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProfileImage(
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(top = 16.dp)
            .clickable { onClick.invoke() }) {
        UserImageCard(
            userImage = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(80.dp)
        )
        Card(
            shape = CircleShape,
            border = BorderStroke(width = 2.dp, color = Color.White),
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp, end = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier.background(color = Color.White)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyPostsScreenPreview() = InstagramTheme {
    MyPosts(
        state = MyPostsViewState.preview(),
        navController = rememberNavController(),
        followers = 0
    )
}