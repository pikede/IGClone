package com.example.instagram.core_ui_components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.instagram.R
import kotlinx.coroutines.delay

enum class AnimationType {
    Like,
    Dislike,
    None
}

private enum class LikeIconSize {
    SMALL,
    LARGE
}

@Composable
fun LikeDislikeAnimation(
    animationType: AnimationType,
    modifier: Modifier = Modifier,
    onAnimationEnd: (() -> Unit)? = null,
) {
    var sizeState by rememberSaveable { mutableStateOf(LikeIconSize.SMALL) }
    var transition = updateTransition(targetState = sizeState, label = "")

    LaunchedEffect(animationType) {
        delay(2000)
        onAnimationEnd?.invoke()
    }

    val size by transition.animateDp(
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

    if (animationType != AnimationType.None) {
        var isLike = animationType == AnimationType.Like
        Image(
            painter = painterResource(id = if (isLike == true) R.drawable.ic_like else R.drawable.ic_dislike),
            contentDescription = if (isLike == true) "like" else "dislike",
            modifier = modifier.size(size = size),
            colorFilter = ColorFilter.tint(color = if (isLike == true) Color.Red else Color.Gray)
        )
    }
    sizeState = LikeIconSize.LARGE
}