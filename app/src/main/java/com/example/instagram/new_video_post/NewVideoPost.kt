package com.example.instagram.new_video_post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import com.example.instagram.DestinationScreen
import com.example.instagram.common.ui.navigation.navigateTo
import com.example.instagram.core_ui_components.BlackTransparentTextContainer
import com.example.instagram.core_ui_components.CommonProgressSpinner
import com.example.instagram.core_ui_components.ShowErrorModal
import com.example.instagram.core_ui_components.ShowEventToast
import com.example.instagram.core_ui_components.video.VideoPlayer
import com.example.instagram.ui.theme.InstagramTheme

@Composable
fun NewVideoPostRoute(
    navController: NavController,
    encodedVideoUri: String,
    modifier: Modifier = Modifier,
) {
    NewVideoPost(
        navController = navController,
        encodedVideoUri = encodedVideoUri,
        modifier = modifier
    )
}

@Composable
private fun NewVideoPost(
    encodedVideoUri: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    vm: NewVideoPostViewModel = hiltViewModel<NewVideoPostViewModel>(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    NewVideoPost(
        encodedVideoUri = encodedVideoUri,
        state = state,
        navController = navController,
        modifier = modifier
    )
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun NewVideoPost(
    encodedVideoUri: String,
    state: NewVideoPostViewState,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier.fillMaxWidth(),
        topBar = { NewVideoPostTopBar(navController, focusManager, state, encodedVideoUri) },
        bottomBar = { NewVidePostFooter(state) }) { paddingValues ->

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            VideoPlayer(videoUri = encodedVideoUri, modifier = Modifier.matchParentSize())
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
private fun NewVidePostFooter(state: NewVideoPostViewState) {
    OutlinedTextField(
        value = state.description.orEmpty(),
        onValueChange = { state.updateDescription(it) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 150.dp), // todo 1/3 of available space
        label = { Text(text = "Description") },
        colors = BlackTransparentTextContainer()
    )
}

@Composable
private fun NewVideoPostTopBar(
    navController: NavController,
    focusManager: FocusManager,
    state: NewVideoPostViewState,
    encodedVideoUri: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Cancel", modifier = Modifier.clickable { navController.popBackStack() })
        Text(text = "Post Video", modifier = Modifier.clickable {
            focusManager.clearFocus()
            state.onPost(encodedVideoUri) {
                navController.popBackStack()
            }
        })
    }
}

@Preview(showBackground = true)
@Composable
private fun NewVideoPostsPreview() = InstagramTheme {
    NewVideoPost(
        state = NewVideoPostViewState.preview(),
        encodedVideoUri = "",
        navController = rememberNavController()
    )
}