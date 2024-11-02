package com.example.instagram.auth

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.instagram.IgViewModel

@Composable
fun SignupScreen(
    navController: NavController,
    vm: IgViewModel,
    modifier: Modifier = Modifier,
) {
    Text(text = "Signup Screen")
}