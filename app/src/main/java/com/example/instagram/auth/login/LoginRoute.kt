package com.example.instagram.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import com.example.instagram.R
import com.example.instagram.common.ui.navigation.CheckSignedIn
import com.example.instagram.core_ui_components.CommonProgressSpinner
import com.example.instagram.core_ui_components.ShowErrorModal
import com.example.instagram.common.ui.navigation.navigateTo
import com.example.instagram.ui.theme.AppTheme

@Composable
fun LoginRoute(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    LoginScreen(navController, modifier)
}

@Composable
private fun LoginScreen(
    navController: NavController,
    modifier: Modifier,
    loginViewmodel: LoginViewmodel = hiltViewModel(),
) {
    val state by loginViewmodel.state.collectAsStateWithLifecycle()
    when {
        state.signedIn -> CheckSignedIn(
            signedIn = state.signedIn,
            navController = navController,
        )

        else -> LoginScreenContent(
            navController = navController,
            state = state,
            modifier = modifier
        )
    }
}

@Composable
private fun LoginScreenContent(
    navController: NavController,
    state: LoginScreenState,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(
                    rememberScrollState()
                ), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ig_logo),
                contentDescription = null,
                modifier = Modifier
                    .width(250.dp)
                    .padding(top = 16.dp)
                    .padding(16.dp)
            )
            Text(
                text = "Login",
                modifier = Modifier.padding(8.dp),
                fontSize = AppTheme.typography.bodyLarge.fontSize,
                fontFamily = FontFamily.SansSerif
            )
            OutlinedTextField(
                value = state.email.orEmpty(),
                onValueChange = { state.updateEmail(it) },
                modifier = Modifier.padding(8.dp),
                label = { Text(text = "Email") },
            )
            OutlinedTextField(
                value = state.password.orEmpty(),
                onValueChange = { state.updatePassword(it) },
                modifier = Modifier.padding(8.dp),
                label = { Text(text = "Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Button(
                onClick = {
                    focusManager.clearFocus(force = true) // clear focus when button is clicked, dismiss keyboard
                    state.login()
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Login")
            }
            Text(
                text = "New here? Go to signup ->",
                color = Color.Blue,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        navigateTo(navController, DestinationScreen.Signup)
                    }
            )
        }
        state.inProgress.takeIf { it }?.let { CommonProgressSpinner() }
    }

    state.error?.let { error ->
        ShowErrorModal(error = error, onDismiss = { state.consumeError() })
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreenContent(rememberNavController(), LoginScreenState.Empty)
}