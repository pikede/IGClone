package com.example.instagram

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.auth.SignupScreenEvent
import com.example.instagram.auth.SignupScreenState
import com.example.instagram.coroutineExtensions.ViewEventSinkFlow
import com.example.instagram.coroutineExtensions.stateInDefault
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class IgViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage,
) : ViewModel() {
    private val default = SignupScreenState()
    private val userName = MutableStateFlow(default.userName)
    private val password = MutableStateFlow(default.password)
    private val email = MutableStateFlow(default.email)

    val state = combine(
        userName,
        password,
        email,
        eventSink(),
        ::SignupScreenState
    ).stateInDefault(viewModelScope, default)

    private fun eventSink(): ViewEventSinkFlow<SignupScreenEvent> = flowOf { event ->
        when (event) {
            SignupScreenEvent.Signup -> signup()
            is SignupScreenEvent.UpdateEmail -> email.value = event.newEmail
            is SignupScreenEvent.UpdateName -> userName.value = event.newName
            is SignupScreenEvent.UpdatePassword -> password.value = event.newPassword
        }
    }

    fun signup() {
        // TODO submit new user info to firebase
    }
}

