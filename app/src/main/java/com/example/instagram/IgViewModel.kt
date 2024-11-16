package com.example.instagram

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.auth.signup.SignupScreenEvent
import com.example.instagram.auth.signup.SignupScreenState
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

val USERS = "users"
val USERNAME = "username"

@HiltViewModel
class IgViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage,
) : ViewModel() {
    private val default = SignupScreenState()
    private val userNameState = MutableStateFlow(default.userName)
    private val passwordState = MutableStateFlow(default.password)
    private val emailState = MutableStateFlow(default.email)
    private val signedInState = MutableStateFlow(default.signedIn)
    private val inProgressState = MutableStateFlow(default.inProgress)
    private val userState = MutableStateFlow(default.user)
    val notificationState = MutableStateFlow(default.notification)
    private val errorState = MutableStateFlow(default.error)

    val state = combine(
        userNameState,
        passwordState,
        emailState,
        signedInState,
        inProgressState,
        userState,
        notificationState,
        errorState,
        eventSink(),
        ::SignupScreenState
    ).stateInDefault(viewModelScope, default)

    init {
//        auth.signOut()
        val currentUser = auth.currentUser
        signedInState.value = currentUser != null
        currentUser?.uid?.let { userId ->
            getUserData(userId)
        }
    }

    private fun eventSink(): ViewEventSinkFlow<SignupScreenEvent> = flowOf { event ->
        when (event) {
            SignupScreenEvent.Signup -> onSignup()
            is SignupScreenEvent.UpdateEmail -> emailState.value = event.newEmail
            is SignupScreenEvent.UpdateName -> userNameState.value = event.newName
            is SignupScreenEvent.UpdatePassword -> passwordState.value = event.newPassword
            SignupScreenEvent.ConsumeError -> errorState.value = null
        }
    }

    fun onSignup() {
        with(state.value) {
            require(!(userName.isNullOrEmpty() or email.isNullOrEmpty() or password.isNullOrEmpty())) {
                errorState.value = Throwable("Please fill in all fields")
                return
            }
        }

        inProgressState.value = true
        db.collection(USERS).whereEqualTo(USERNAME, userNameState.value.orEmpty()).get()
            .addOnSuccessListener { documents ->
                when {
                    documents.size() > 0 -> {
                        errorState.value = Throwable("Username already exists")
                        inProgressState.value = false
                    }

                    else -> {
                        auth.createUserWithEmailAndPassword(
                            emailState.value.orEmpty(),
                            passwordState.value.orEmpty()
                        ).addOnCompleteListener { task ->
                            when {
                                task.isSuccessful -> {
                                    signedInState.value = true
                                    createOrUpdateProfile(username = userNameState.value)
                                }

                                else -> errorState.value = task.exception
                            }
                            inProgressState.value = false
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                errorState.value = exception
                inProgressState.value = false
            }
    }

    private fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        imageUrl: String? = null,
    ) {
        val uid = auth.currentUser?.uid
        val userData = User(
            userId = uid,
            name = name ?: userState.value?.name,
            userName = username ?: userState.value?.userName,
            bio = bio ?: userState.value?.bio,
            imageUrl = imageUrl ?: userState.value?.imageUrl,
            following = userState.value?.following
        )
        uid?.let {
            inProgressState.value = true
            db.collection(USERS).document(uid).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    document.reference.update(userData.toMap())
                        .addOnSuccessListener {
                            userState.value = userData
                            inProgressState.value = false
                        }
                        .addOnFailureListener {
                            errorState.value = Throwable(it)
                            Log.e(
                                "*** Failed to createOrUpdateProfile",
                                it.localizedMessage.orEmpty()
                            )
                            inProgressState.value = false
                        }
                } else {
                    db.collection(USERS).document(uid).set(userData)
                    getUserData(uid)
                    inProgressState.value = false
                }
            }.addOnFailureListener {
                errorState.value = it
                Log.e("*** Failed to createOrUpdateProfile", it.localizedMessage.orEmpty())
                inProgressState.value = false
            }
        }
    }

    private fun getUserData(uid: String) {
        inProgressState.value =
            true // TODO make there's no issue when this is reached as false is called when @getUserData is called
        db.collection(USERS).document(uid)
            .get()  // TODO create a helper that gets the document from firebase
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                userState.value = user
                inProgressState.value = false
            }
            .addOnFailureListener {
                errorState.value = it
                Log.e("*** Failed to createOrUpdateProfile", it.localizedMessage.orEmpty())
            }
    }
}


