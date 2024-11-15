package com.example.instagram.core_ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// TODO create notification toast
@Composable
fun ShowErrorModal(
    error: Throwable,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    customMessage: String = "",
) {
    val errorMsg = error.localizedMessage ?: ""
    val message = if (customMessage.isEmpty()) errorMsg else "$customMessage: $errorMsg"
    val context = LocalContext.current

    Box(modifier = modifier.clickable { onDismiss() }) {
        Toast.makeText(context, message, Toast.LENGTH_LONG)
            .show() // todo replace with clickable error modal
    }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(error, onDismiss, coroutineScope) {
            onDismiss()
    }
}

