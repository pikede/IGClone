package com.example.instagram.core_ui_components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.instagram.common.extensions.OneTimeEvent
import kotlinx.coroutines.delay

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(text = message, modifier = Modifier.padding(25.dp))
    }

    LaunchedEffect(error, onDismiss) {
        delay(4000)
        onDismiss()
    }
}

@Preview
@Composable
private fun ShowErrorModalPreview() {
    ShowErrorModal(
        error = Throwable("Error"),
        customMessage = "Custom Error Message"
    )
}
