package com.example.instagram.core_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LoadingIconSize = 32.dp

@Composable
fun ProgressSpinner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .alpha(0.5f)
            .background(Color.LightGray)
            .clickable(enabled = false) {}
            .fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun FullscreenLoading(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    icon: @Composable (Modifier) -> Unit = {},
    content: @Composable BoxScope.() -> Unit = { },
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        content()
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Cyan)
            ) {
                icon(
                    Modifier
                        .size(LoadingIconSize)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

// TODO add if then else condition for applying and chaining modifiers
