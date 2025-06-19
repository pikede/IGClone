package com.example.instagram.my_posts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.instagram.DestinationScreen
import com.example.instagram.R
import com.example.instagram.common.ui.navigation.BottomNavigationItem
import com.example.instagram.common.ui.navigation.BottomNavigationMenu
import com.example.instagram.common.ui.navigation.navigateTo
import com.example.instagram.common.util.Constants.IMAGE_URI
import com.example.instagram.core_ui_components.images.CommonImage
import com.example.instagram.core_ui_components.CommonProgressSpinner
import com.example.instagram.core_ui_components.images.UserImageCard
import com.example.instagram.models.PostData
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

    MyPostsScreen(
        state = state,
        navController = navController,
        followers = followers,
        modifier = modifier,
    )

    LaunchedEffect(Unit) {// todo reload on resume here
        vm.refreshPosts()
    }
}

@Composable
private fun MyPostsScreen(
    state: MyPostsViewState,
    navController: NavController,
    followers: Int,
    modifier: Modifier = Modifier,
) {
    val newPostImageLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val route = DestinationScreen.NewPost(it.toString())
                navController.navigate(route)
            }
        }
    Column(modifier) {
        Column(modifier = Modifier.weight(1f)) {
            Row {
                ProfileImage(imageUrl = state.user?.imageUrl, onClick = {
                    newPostImageLauncher.launch(IMAGE_URI) // checks for any type of image on the device
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
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = state.user?.name.orEmpty(), fontWeight = FontWeight.Bold)
                Text(text = state.getUserName())
                Text(text = state.user?.bio.orEmpty())
            }
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

@Composable
fun PostList(
    isContextLoading: Boolean,
    postsLoading: Boolean,
    posts: List<PostData>,
    modifier: Modifier = Modifier,
    onPostClick: (PostData) -> Unit,
) {
    if (postsLoading) {
        CommonProgressSpinner()
    } else if (posts.isEmpty()) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isContextLoading) Text(text = "No posts available")
        }
    } else {
        LazyColumn(modifier = modifier) {
            val rows = arrayListOf<PostRow>()
            var currentRow = PostRow()
            rows.add(currentRow)
            for (post in posts) {
                if (currentRow.isFull()) {
                    currentRow = PostRow()
                    rows.add(currentRow)
                }
                currentRow.addPost(post)
            }
            items(items = rows) { row ->
                PostsRow(postRow = row, onPostClick = onPostClick)
            }
        }
    }
}

@Composable
fun PostsRow(
    postRow: PostRow,
    onPostClick: (PostData) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        PostImage(
            imageUrl = postRow.post1?.postImage,
            modifier = Modifier
                .weight(1f)
                .clickable { postRow.post1?.let { post -> onPostClick(post) } })
        PostImage(
            imageUrl = postRow.post2?.postImage,
            modifier = Modifier
                .weight(1f)
                .clickable { postRow.post2?.let { post -> onPostClick(post) } })
        PostImage(
            imageUrl = postRow.post3?.postImage,
            modifier = Modifier
                .weight(1f)
                .clickable { postRow.post3?.let { post -> onPostClick(post) } })
    }
}

@Composable
fun PostImage(imageUrl: String?, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        var modifier = Modifier
            .padding(1.dp)
            .fillMaxSize()

        if (imageUrl == null) {
            modifier = modifier.clickable(enabled = false) {}
        }

        CommonImage(
            data = imageUrl,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MyPostsScreenPreview() = InstagramTheme {
    MyPostsScreen(
        state = MyPostsViewState.preview(),
        navController = rememberNavController(),
        followers = 0
    )
}