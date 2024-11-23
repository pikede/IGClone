package com.example.instagram.my_posts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.USERS
import com.example.instagram.core_domain.ViewEventSinkFlow
import com.example.instagram.coroutineExtensions.combine
import com.example.instagram.coroutineExtensions.stateInDefault
import com.example.instagram.entities.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
internal class MyPostsViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
) : ViewModel() {
    private val default = MyPostsViewState.Companion.Empty
    private val inProgressState = MutableStateFlow(default.inProgress)
    private val userState = MutableStateFlow(default.user)
    val notificationState = MutableStateFlow(default.notification)
    private val errorState = MutableStateFlow(default.error)

    val state = combine(
        inProgressState,
        userState,
        notificationState,
        errorState,
        eventSink(),
        ::MyPostsViewState
    ).stateInDefault(viewModelScope, default)

    init {
        val currentUser = auth.currentUser
        currentUser?.uid?.let { userId ->
            getUserData(userId) // todo cash getUserData after getting with interactor value
        }
    }

    private fun getUserData(uid: String) {
        inProgressState.value =
            true // TODO make there's no issue when this is reached as false is called when @getUserData is called
        db.collection(USERS).document(uid)
            .get()  // TODO create a helper that gets the document from firebase in an Interactor
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

    private fun eventSink(): ViewEventSinkFlow<MyPostsScreenEvent> = flowOf { event ->
        when (event) {
            MyPostsScreenEvent.ConsumeError -> errorState.value = null
        }
    }
}