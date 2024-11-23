package com.example.instagram.core_ui

import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun BlackTextTransparentContainer(
    textColor: Color = Color.Black,
    backgroundColor: Color = Color.Transparent,
): TextFieldColors = TextFieldDefaults.colors(
    focusedContainerColor = backgroundColor,
    unfocusedContainerColor = backgroundColor,
    focusedTextColor = textColor,
    unfocusedTextColor = textColor,
    focusedLabelColor = textColor,
    unfocusedLabelColor = textColor
)