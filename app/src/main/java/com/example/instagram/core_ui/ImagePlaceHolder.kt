package com.example.instagram.core_ui

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.example.instagram.R

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ImagePlaceHolder(
    data: String?,
    modifier: Modifier = Modifier.wrapContentSize(),
    contentScale: ContentScale = ContentScale.Crop,
) {
    val painter = rememberImagePainter(data = data)
    Image(
        painter = painter,
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier,
    )
    if (painter.state is ImagePainter.State.Loading) {
        ProgressSpinner()
    }
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
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = Color.Gray)
                )
            }

            else -> ImagePlaceHolder(data = userImage)
        }
    }
}

@Preview
@Composable
private fun UserImageCardPreview() {
    UserImageCard(userImage = "")
}