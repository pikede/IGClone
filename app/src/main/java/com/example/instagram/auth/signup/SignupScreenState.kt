package com.example.instagram.auth.signup

import androidx.compose.runtime.Immutable
import com.example.instagram.common.extensions.OneTimeEvent
import com.example.instagram.common.extensions.ViewEventSink
import com.example.instagram.entities.User

@Immutable
internal data class SignupScreenState(
    val userName: String? = null,
    val password: String? = null,
    val email: String? = null,
    val signedIn: Boolean = false,
    val inProgress: Boolean = false,
    val user: User? = null,
    val notification: OneTimeEvent<String>? = null,
    val error: Throwable? = null,  // TODO create custom throwable type with it's invokable composable that takes in a custom message
    val eventSink: ViewEventSink<SignupScreenEvent> = {},
) {
    fun signup() {
        eventSink(SignupScreenEvent.Signup)
    }

    fun consumeError() {
        eventSink(SignupScreenEvent.ConsumeError)
    }

    companion object {
        val Empty = SignupScreenState()
    }
}

sealed interface SignupScreenEvent {
    data class UpdateName(val newName: String) : SignupScreenEvent
    data class UpdateEmail(val newEmail: String) : SignupScreenEvent
    data class UpdatePassword(val newPassword: String) : SignupScreenEvent
    object Signup : SignupScreenEvent
    object ConsumeError : SignupScreenEvent
}
