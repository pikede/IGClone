package com.example.instagram.new_post

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.instagram.DestinationScreen
import com.example.instagram.common.ui.navigation.navigateTo
import com.example.instagram.core_ui_components.BlackTransparentTextContainer
import com.example.instagram.core_ui_components.CommonDivider
import com.example.instagram.core_ui_components.CommonProgressSpinner
import com.example.instagram.core_ui_components.ShowErrorModal
import com.example.instagram.core_ui_components.ShowEventToast
import com.example.instagram.ui.theme.InstagramTheme

@Composable
fun NewPostRoute(
    navController: NavController,
    encodedUri: String,
    modifier: Modifier = Modifier,
) {
    NewPost(navController = navController, encodedUri = encodedUri, modifier = modifier)
}

@Composable
private fun NewPost(
    encodedUri: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    vm: NewPostViewModel = hiltViewModel<NewPostViewModel>(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    NewPostScreen(
        encodedUri = encodedUri,
        state = state,
        navController = navController,
        modifier = modifier
    )
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun NewPostScreen(
    encodedUri: String,
    state: NewPostViewState,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Cancel", modifier = Modifier.clickable { navController.popBackStack() })
            Text(text = "Post Image", modifier = Modifier.clickable {
                focusManager.clearFocus()
                state.onPost(encodedUri) {
                    navController.popBackStack()
                }
            })
        }

        CommonDivider()

        NewImagePost(
            imageUri = encodedUri,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp, max = 600.dp)
        )

        Row(modifier = Modifier.padding(8.dp)) {
            OutlinedTextField(
                value = state.description.orEmpty(),
                onValueChange = { state.updateDescription(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                label = { Text(text = "Description") },
                singleLine = false,
                colors = BlackTransparentTextContainer()
            )
        }
    }
    if (state.inProgress) {
        CommonProgressSpinner()
    }
    if (state.isSignedIn.not()) {
        navigateTo(navController, DestinationScreen.Login)
    }
    state.error?.let { error ->
        ShowErrorModal(error = error, onDismiss = { state.consumeError() })
    }
    state.notification?.ShowEventToast()
}

@Composable
private fun NewImagePost(imageUri: String, modifier: Modifier = Modifier) {
    Image(
        painter = rememberImagePainter(imageUri),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.FillWidth
    )
}


@Preview(showBackground = true)
@Composable
private fun NewPostsScreenPreview() = InstagramTheme {
    NewPostScreen(
        state = NewPostViewState.preview(),
        encodedUri = "",
        navController = rememberNavController()
    )
}