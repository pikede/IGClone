package com.example.instagram.core_ui_components.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.instagram.R
import com.example.instagram.core_ui_components.CommonProgressSpinner

@OptIn(ExperimentalCoilApi::class)
@Composable
fun CommonImage(
    data: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val painter = rememberImagePainter(data = data)
    Image(
        painter = painter,
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier.wrapContentSize(),
    )
    if (painter.state is ImagePainter.State.Loading) {
        CommonProgressSpinner()
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun CommonAsyncImage(
    data: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale,
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(data)
            .memoryCacheKey(data)
            .diskCacheKey(data)
            .crossfade(true)
            .build(),
        placeholder = painterResource(R.drawable.ic_launcher_foreground),
        error = painterResource(R.drawable.ic_launcher_foreground),
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier,
    )
}

@Composable
fun UserImageCard(
    userImage: String?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .size(64.dp)
            .padding(8.dp),
        shape = CircleShape,
    ) {
        when {
            userImage.isNullOrEmpty() -> {
                Image(
                    painter = painterResource(R.drawable.ic_user),
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = Color.Gray)
                )
            }

            else -> CommonImage(data = userImage)
        }
    }
}

@Preview
@Composable
private fun UserImageCardPreview() {
    UserImageCard(userImage = "")
}