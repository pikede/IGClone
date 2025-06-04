package com.example.instagram.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.common.extensions.ViewEventSinkFlow
import com.example.instagram.coroutineExtensions.combine
import com.example.instagram.coroutineExtensions.stateInDefault
import com.example.instagram.domain.InvalidUserException
import com.example.instagram.domain.UserNotFoundException
import com.example.instagram.domain.interactors.GetUser
import com.example.instagram.domain.interactors.SignIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewmodel @Inject constructor(
    private val getUser: GetUser,
    private val signIn: SignIn,
) : ViewModel() {
    private val default = LoginScreenState.Empty
    private val emailState = MutableStateFlow(default.email)
    private val passwordState = MutableStateFlow(default.password)
    private val signedInState = MutableStateFlow(default.signedIn)
    private val inProgressState = MutableStateFlow(default.inProgress)
    private val errorState = MutableStateFlow(default.error)

    val state = combine(
        emailState,
        passwordState,
        signedInState,
        inProgressState,
        errorState,
        eventSink(),
        ::LoginScreenState
    ).stateInDefault(viewModelScope, default)

    init {
        getExistingUser()
    }

    private fun eventSink(): ViewEventSinkFlow<LoginScreenEvent> = flowOf { event ->
        when (event) {
            is LoginScreenEvent.UpdateEmail -> emailState.value = event.newEmail
            is LoginScreenEvent.UpdatePassword -> passwordState.value = event.newPassword
            LoginScreenEvent.ConsumeError -> errorState.value = null
            LoginScreenEvent.Login -> onLogin()
        }
    }

    private fun onLogin() = viewModelScope.launch {
        if (emailState.value.isNullOrEmpty() || passwordState.value.isNullOrEmpty()) {
            errorState.value = Throwable("Please fill in all fields")
            return@launch
        }
        inProgressState.value = true
        signIn.getResult(SignIn.Params(emailState.value.orEmpty(), passwordState.value.orEmpty()))
            .onSuccess {
                signedInState.value = true
                Log.d("*** LoginViewmodel", "onLogin: Login Successful")
            }.onFailure { errorState.value = it }

        inProgressState.value = false
    }

    private fun getExistingUser() = viewModelScope.launch {
        inProgressState.value = true
        getUser.getResult()
            .onSuccess { signedInState.value = true }
            .onFailure {
                Log.e("*** LoginViewmodel", "getUserData: ${it.message}")
                val expectedCauses = setOf(UserNotFoundException, InvalidUserException)
                if (it !in expectedCauses) { // ignoring these errors as a different user may be logging in
                    errorState.value = it
                }
            }
        inProgressState.value = false
    }
}