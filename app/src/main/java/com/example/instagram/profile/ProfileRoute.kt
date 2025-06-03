package com.example.instagram.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.instagram.DestinationScreen
import com.example.instagram.common.ui.navigation.navigateTo
import com.example.instagram.common.util.Constants.IMAGE_URI
import com.example.instagram.core_ui_components.BlackTransparentTextContainer
import com.example.instagram.core_ui_components.CommonDivider
import com.example.instagram.core_ui_components.CommonImage
import com.example.instagram.core_ui_components.CommonProgressSpinner
import com.example.instagram.core_ui_components.FullscreenLoading

// todo rename package and files to edit profile
@Composable
fun ProfileRoute(navController: NavController, modifier: Modifier = Modifier) {
    Profile(navController = navController, modifier = modifier)
}

@Composable
private fun Profile(
    navController: NavController,
    modifier: Modifier = Modifier,
    vm: ProfileViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        navController = navController,
        modifier = modifier
    )
}

@Composable
fun ProfileScreen(
    state: ProfileViewState,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    FullscreenLoading(isLoading = state.inProgress, modifier = modifier) {
        ProfileScreenContent(
            state = state,
            onBack = { navController.popBackStack() },
            onSave = { state.onSave() }, // todo tell previous composable to reload when saving
            onLogout = {
                state.onLogout()
                navigateTo(navController, DestinationScreen.Login)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    state: ProfileViewState,
    onBack: () -> Boolean,
    onSave: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Back", modifier = Modifier.clickable { onBack.invoke() })
            Text(text = "Save", modifier = Modifier.clickable { onSave.invoke() })
        }

        CommonDivider()

        ProfileImage(state = state)

        CommonDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Name:", modifier = Modifier.width(100.dp))
            TextField(
                value = state.user?.name.orEmpty(),
                onValueChange = { it -> state.updateName(it) },
                colors = BlackTransparentTextContainer(),
                singleLine = true
            )
        }
        CommonDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Username:", modifier = Modifier.width(100.dp))
            TextField(
                value = state.user?.userName.orEmpty(),
                onValueChange = state::updateUserName,
                colors = BlackTransparentTextContainer(),
                singleLine = true
            )
        }

        CommonDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = "Bio", modifier = Modifier.width(100.dp))
            TextField(
                value = state.user?.bio.orEmpty(),
                onValueChange = state::updateBio,
                colors = BlackTransparentTextContainer(),
                singleLine = false,
                modifier = Modifier.height(150.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(text = "Logout", modifier = Modifier.clickable { onLogout.invoke() })
        }
    }
}

@Composable
private fun ProfileImage(
    state: ProfileViewState,
    modifier: Modifier = Modifier,
) {
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                state.updateProfileImageUrl(it)
            }
        }
    Box(modifier = modifier.height(IntrinsicSize.Min)) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable { launcher.launch(IMAGE_URI) },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp)
            ) {
                CommonImage(data = state.user?.imageUrl)
            }
            Text(text = "Change profile picture")
        }
        if (state.inProgress) {
            CommonProgressSpinner()
        }
    }
}