package com.example.instagram.core_ui_components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.instagram.common.extensions.OneTimeEvent

// TODO create notification toast
@Composable
fun <T> OneTimeEvent<T>.ShowEventToast(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    LaunchedEffect(this.hasBeenHandled) {
        if (this@ShowEventToast.hasBeenHandled.not()) {
            Toast.makeText(context, this@ShowEventToast().toString(), Toast.LENGTH_LONG)
                .show()
        }
    }
}

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

