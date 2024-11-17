package com.example.instagram.auth.login

import androidx.compose.runtime.Immutable
import com.example.instagram.coroutineExtensions.ViewEventSink
import com.example.instagram.entities.User

@Immutable
data class LoginScreenState(
    val email: String? = null,
    val password: String? = null,
    val signedIn: Boolean = false,
    val inProgress: Boolean = false,
    val user: User? = null,
    val error: Throwable? = null,
    val eventSink: ViewEventSink<LoginScreenEvent> = {},
) {
    fun consumeError() {
        eventSink(LoginScreenEvent.ConsumeError)
    }

    fun login() {
        eventSink(LoginScreenEvent.Login)
    }

    fun updateEmail(newEmail: String) {
        eventSink(LoginScreenEvent.UpdateEmail(newEmail))
    }

    fun updatePassword(newPassword: String) {
        eventSink(LoginScreenEvent.UpdatePassword(newPassword))
    }

    companion object {
        val Empty = LoginScreenState()
        // todo move preview here with eventsync events
    }
}

sealed interface LoginScreenEvent {
    data class UpdateEmail(val newEmail: String) : LoginScreenEvent
    data class UpdatePassword(val newPassword: String) : LoginScreenEvent
    object Login : LoginScreenEvent
    object ConsumeError : LoginScreenEvent
}