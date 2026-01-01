package com.example.instagram.single_post.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import com.example.instagram.core_ui_components.extensions.detectTransformGesturesEnd
import com.example.instagram.core_ui_components.image.CommonImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// https://developer.android.com/reference/kotlin/androidx/compose/foundation/gestures/package-summary#(androidx.compose.ui.input.pointer.PointerInputScope).detectTransformGestures(kotlin.Boolean,kotlin.Function4)
// https://medium.com/@baljindermaan15/creating-image-zoom-in-and-out-in-jetpack-compose-a8b16ad8d2dc
@Composable
internal fun ColumnScope.SinglePostImage(
    postImage: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset(0f, 0f)) }
        val coroutineScope = rememberCoroutineScope()
        val modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTransformGesturesEnd(
                    onGesture = { _, pan, zoom, _ ->
                        // Update the scale based on zoom gestures.
                        scale *= zoom

                        // Limit the zoom levels within a certain range (optional).
                        scale = scale.coerceIn(0.5f, 3f)

                        // Update the offset to implement panning when zoomed.
                        offset = if (scale == 1f) Offset(0f, 0f) else offset + pan
                    },
                    // resets image back to original size after, 1 second after the gesture has ended
                    onGestureEnd = {
                        coroutineScope.launch {
                            delay(1000)
                            scale = 1f
                        }
                    }
                )
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
        CommonImage(
            data = postImage,
            modifier = modifier,
            contentScale = ContentScale.FillWidth
        )
    }
}
