package com.example.instagram.core_ui_components

import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun BlackTransparentTextContainer(
    textColor: Color = Color.Black,
    backgroundColor: Color = Color.Transparent,
    focusedLabelColor: Color = Color.Black,
    unfocusedLabelColor: Color = Color.Black,
    disabledIndicatorColor: Color = Color.Black,
    focusedIndicatorColor: Color = Color.Transparent,
    unfocusedIndicatorColor: Color = Color.Transparent,
): TextFieldColors = TextFieldDefaults.colors(
    focusedContainerColor = backgroundColor,
    unfocusedContainerColor = backgroundColor,
    focusedTextColor = textColor,
    unfocusedTextColor = textColor,
    focusedLabelColor = focusedLabelColor,
    unfocusedLabelColor = unfocusedLabelColor,
    disabledIndicatorColor = disabledIndicatorColor,
    focusedIndicatorColor = focusedIndicatorColor,
    unfocusedIndicatorColor = unfocusedIndicatorColor,
)