package com.example.instagram.my_posts.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.instagram.core_ui_components.image.CommonImage
import com.example.instagram.core_ui_components.video.VideoPlayer
import com.example.instagram.models.PostData

@Composable
internal fun MyPostsItems(
    // todo move to core UI components
    posts: List<PostData>,
    onPostClick: (PostData) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        val rows = posts.chunked(3)
        items(items = rows) { postItems ->
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                postItems.forEach {
                    PostRow(
                        it, Modifier
                            .weight(1f)
                            .clickable { onPostClick(it) })
                }
            }
        }
    }
}

@Composable
private fun PostRow(
    postItem: PostData,
    modifier: Modifier,
) {
    when {
        postItem.postVideo != null -> VideoPlayer(
            videoUri = postItem.postVideo,
            playWhenReady = false,
            modifier = modifier
        )

        else -> {
            PostImage(
                imageUrl = postItem.postImage,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun PostImage(imageUrl: String?, modifier: Modifier = Modifier) {
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
fun MyPostsItemsPreview() {
    val samplePosts = listOf(
        PostData(postImage = "https://example.com/image1.jpg"),
        PostData(postVideo = "https://example.com/video1.mp4"),
        PostData(postImage = "https://example.com/image2.jpg"),
        PostData(postImage = "https://example.com/image3.jpg"),
        PostData(postVideo = "https://example.com/video2.mp4"),
        PostData(postImage = "https://example.com/image4.jpg")
    )
    MyPostsItems(
        posts = samplePosts,
        modifier = Modifier.fillMaxSize(),
        onPostClick = {}
    )
}