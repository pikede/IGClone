package com.example.instagram.core_ui_components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.instagram.R

private enum class LikeIconSize {
    SMALL,
    LARGE
}

@Composable
fun LikeAnimation(isLike: Boolean = true, modifier: Modifier = Modifier) {
    var sizeState by remember { mutableStateOf(LikeIconSize.SMALL) }
    var transition = updateTransition(targetState = sizeState, label = "")
    val size by transition.animateDp(
        label = "",
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        }) { state ->
        when (state) {
            LikeIconSize.SMALL -> 0.dp
            LikeIconSize.LARGE -> 150.dp
        }
    }

    Image(
        painter = painterResource(id = if (isLike) R.drawable.ic_like else R.drawable.ic_dislike),
        contentDescription = "",
        modifier = modifier.size(size = size),
        colorFilter = ColorFilter.tint(color = if (isLike) Color.Red else Color.Gray)
    )
    sizeState = LikeIconSize.LARGE
}