package com.example.instagram.auth.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.instagram.DestinationScreen
import com.example.instagram.IgViewModel
import com.example.instagram.R
import com.example.instagram.core_ui.ProgressSpinner
import com.example.instagram.core_ui.ShowErrorModal
import com.example.instagram.core_ui.ShowEventToast
import com.example.instagram.core_ui.navigation.CheckSignedIn
import com.example.instagram.core_ui.navigation.navigateTo
import com.example.instagram.ui.theme.AppTheme

@Composable
fun SignupRoute(
    navController: NavController,
    modifier: Modifier = Modifier,
    vm: IgViewModel = hiltViewModel(),
) {
    SignupScreen(vm, navController, modifier)
}

@Composable
private fun SignupScreen(
    vm: IgViewModel,
    navController: NavController,
    modifier: Modifier,
) {
    val state by vm.state.collectAsStateWithLifecycle()

    when {
        state.signedIn -> CheckSignedIn(
            signedIn = state.signedIn,
            navController = navController,
        )

        else -> SignUpScreenContent(
            navController = navController,
            state = state,
            modifier = modifier
        )
    }
}

@Composable
private fun SignUpScreenContent(
    navController: NavController,
    state: SignupScreenState,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ig_logo),
                contentDescription = null,
                modifier = Modifier
                    .width(250.dp)
                    .padding(top = 16.dp)
                    .padding(8.dp)
            )
            Text(
                text = "SignUp",
                modifier = Modifier.padding(8.dp),
                fontSize = AppTheme.typography.titleLarge.fontSize,
                fontFamily = FontFamily.SansSerif
            )
            OutlinedTextField(
                value = state.userName.orEmpty(),
                onValueChange = { state.eventSink(SignupScreenEvent.UpdateName(it)) },
                modifier = Modifier.padding(8.dp),
                label = { Text(text = "Username") }
            )
            OutlinedTextField(
                value = state.email.orEmpty(),
                onValueChange = { state.eventSink(SignupScreenEvent.UpdateEmail(it)) },
                modifier = Modifier.padding(8.dp),
                label = { Text(text = "Email") },
            )
            OutlinedTextField(
                value = state.password.orEmpty(),
                onValueChange = { state.eventSink(SignupScreenEvent.UpdatePassword(it)) },
                modifier = Modifier.padding(8.dp),
                label = { Text(text = "Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Button(
                onClick = {
                    state.signup()
                    focusManager.clearFocus(force = true)// clear focus when button is clicked, dismiss keyboard /
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Sign Up")
            }
            Text(
                text = "Already a user? Go to login ->",
                color = Color.Blue,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        navigateTo(navController, DestinationScreen.Login)
                    }
            )
        }
        state.inProgress.takeIf { it }?.let { ProgressSpinner() }
    }

    state.error?.let { error ->
        ShowErrorModal(error = error, onDismiss = { state.consumeError() })
    }
    state.notification?.ShowEventToast()
}

@Preview
@Composable
private fun SignupScreenPreview() {
    SignUpScreenContent(rememberNavController(), SignupScreenState.Empty)
}