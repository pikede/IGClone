package com.example.instagram.single_post.components

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import com.example.instagram.core_ui_components.images.CommonImage
import com.example.instagram.single_post.SinglePostViewState

// https://developer.android.com/reference/kotlin/androidx/compose/foundation/gestures/package-summary#(androidx.compose.ui.input.pointer.PointerInputScope).detectTransformGestures(kotlin.Boolean,kotlin.Function4)
// https://medium.com/@baljindermaan15/creating-image-zoom-in-and-out-in-jetpack-compose-a8b16ad8d2dc
@Composable
internal fun ColumnScope.SinglePostImage(state: SinglePostViewState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        var scale by remember { mutableFloatStateOf(1f) }

        val modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTransformGestures(
                    onGesture = { _, _, zoom, _ ->
                        // Update the scale based on zoom gestures.
                        scale *= zoom

                        // Limit the zoom levels within a certain range (optional).
                        scale = scale.coerceIn(0.5f, 3f)
                    }
                )
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
            )
        CommonImage(
            data = state.postData?.postImage,
            modifier = modifier,
            contentScale = ContentScale.FillWidth
        )
    }
}
