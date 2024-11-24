package com.example.instagram.core_ui_components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CommonDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        color = Color.LightGray,
        thickness = 1.dp,
        modifier = modifier
            .alpha(0.3f)
            .padding(top = 8.dp, bottom = 8.dp)
    )
}