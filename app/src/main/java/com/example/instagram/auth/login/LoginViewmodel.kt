package com.example.instagram.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.USERS
import com.example.instagram.coroutineExtensions.ViewEventSinkFlow
import com.example.instagram.coroutineExtensions.combine
import com.example.instagram.coroutineExtensions.stateInDefault
import com.example.instagram.entities.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class LoginViewmodel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
) : ViewModel() {
    val default = LoginScreenState.EMPTY
    val emailState = MutableStateFlow(default.email)
    val passwordState = MutableStateFlow(default.password)
    val signedInState = MutableStateFlow(default.signedIn)
    val userState = MutableStateFlow(default.user)
    val inProgressState = MutableStateFlow(default.inProgress)
    val errorState = MutableStateFlow(default.error)

    val state = combine(
        emailState,
        passwordState,
        signedInState,
        inProgressState,
        userState,
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
        with(state.value) {
            require(!(emailState.value.isNullOrEmpty() or passwordState.value.isNullOrEmpty())) {
                errorState.value = Throwable("Please fill in all fields")
                return
            }
        }
        inProgressState.value = true
        auth.signInWithEmailAndPassword(emailState.value.orEmpty(), passwordState.value.orEmpty())
            .addOnCompleteListener { task ->
                when {
                    task.isSuccessful -> {
                        signedInState.value = true
                        auth.currentUser?.uid?.let { userId ->
                            getUserData(userId)
                            errorState.value = Throwable("Login Successful")
                        }
                    }

                    else -> errorState.value = task.exception
                }
                inProgressState.value = false
            }
            .addOnFailureListener {
                errorState.value = it
                inProgressState.value = false
            }
    }

    // TODO move to interactor and remove the duplicate method in @IgViewmodel
    private fun getUserData(uid: String) {
        inProgressState.value = true // TODO make there's no issue when this is reached as false is called when @getUserData is called
        db.collection(USERS).document(uid)
            .get()  // TODO create a helper that gets the document from firebase in an Interactor
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                userState.value = user
                inProgressState.value = false
            }
            .addOnFailureListener {
                errorState.value = it
                Log.e("*** Failed to getData after login", it.localizedMessage.orEmpty())
            }
    }
}