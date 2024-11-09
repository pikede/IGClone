package com.example.instagram.auth

import androidx.compose.runtime.Immutable
import com.example.instagram.coroutineExtensions.ViewEventSink

@Immutable
data class SignupScreenState(
    val userName: String? = null,
    val password: String? = null,
    val email: String? = null,
    val eventSink: ViewEventSink<SignupScreenEvent> = {},
) {
    fun signup() {
        eventSink(SignupScreenEvent.Signup)
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
}
