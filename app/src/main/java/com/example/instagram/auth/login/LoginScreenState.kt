package com.example.instagram.auth.login

import androidx.compose.runtime.Immutable
import com.example.instagram.coroutineExtensions.ViewEventSink

@Immutable
data class LoginScreenState(
    val email: String? = null,
    val password: String? = null,
    val inProgress: Boolean = false,
    val error: Throwable? = null,
    val eventSink: ViewEventSink<LoginScreenEvent> = {},
) {
    fun consumeError() {

    }

    fun login() {

    }

    fun updateEmail(newEmail: String) {
        eventSink(LoginScreenEvent.UpdateEmail(newEmail))
    }

    fun updatePassword(newPassword: String) {
        eventSink(LoginScreenEvent.UpdatePassword(newPassword))
    }

    companion object {
        val EMPTY = LoginScreenState()
        // todo move previer here with eventsync events
    }
}

sealed interface LoginScreenEvent {
    data class UpdateEmail(val newEmail: String) : LoginScreenEvent
    data class UpdatePassword(val newPassword: String) : LoginScreenEvent
    object Login : LoginScreenEvent
    object ConsumeError : LoginScreenEvent
}