package com.example.instagram.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.coroutineExtensions.ViewEventSinkFlow
import com.example.instagram.coroutineExtensions.combine
import com.example.instagram.coroutineExtensions.stateInDefault
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class LoginViewmodel : ViewModel() {
    val default = LoginScreenState.EMPTY
    val emailState = MutableStateFlow(default.email)
    val passwordState = MutableStateFlow(default.password)
    val inProgressState = MutableStateFlow(default.inProgress)
    val errorState = MutableStateFlow(default.error)

    val state = combine(
        emailState,
        passwordState,
        inProgressState,
        errorState,
        eventSink(),
        ::LoginScreenState
    ).stateInDefault(viewModelScope, default)


    private fun eventSink(): ViewEventSinkFlow<LoginScreenEvent> = flowOf { event ->
        when (event) {
            is LoginScreenEvent.UpdateEmail -> emailState.value = event.newEmail
            is LoginScreenEvent.UpdatePassword -> passwordState.value = event.newPassword
            LoginScreenEvent.ConsumeError -> errorState.value = null
            LoginScreenEvent.Login -> onLogin()
        }
    }

    private fun onLogin() {

    }
}