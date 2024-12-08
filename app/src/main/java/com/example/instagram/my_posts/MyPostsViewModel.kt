package com.example.instagram.my_posts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.common.extensions.OneTimeEvent
import com.example.instagram.common.extensions.ViewEventSinkFlow
import com.example.instagram.common.util.Constants.POSTS
import com.example.instagram.common.util.Constants.USERS
import com.example.instagram.coroutineExtensions.combine
import com.example.instagram.coroutineExtensions.stateInDefault
import com.example.instagram.entities.User
import com.example.instagram.new_post.PostData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
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
    private val refreshPostsProgressState = MutableStateFlow(default.refreshPostsProgress)
    private val postsState = MutableStateFlow(default.posts)
    private val isSignedInState = MutableStateFlow(default.isSignedIn)

    val state = combine(
        inProgressState,
        userState,
        notificationState,
        errorState,
        refreshPostsProgressState,
        postsState,
        isSignedInState,
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
                refreshPosts()
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

    // todo move to Interactor
    internal fun refreshPosts() {
        val currentUid = auth.currentUser?.uid
        currentUid?.let {
            refreshPostsProgressState.value = true
            db.collection(POSTS)
                .whereEqualTo("userId", currentUid).get()
                .addOnSuccessListener { documents ->
                    convertPosts(documents)
                }.addOnFailureListener {
                    errorState.value = it
                    notificationState.value = OneTimeEvent("Cannot fetch posts")
                }
            refreshPostsProgressState.value = false
        } ?: run {
            onLogout()
            errorState.value = Throwable("Error: username unavailable. Unable to refresh posts")
        }
    }

    // todo move create Interactor for this
    private fun convertPosts(documents: QuerySnapshot) {
        val newPosts = mutableListOf<PostData>()
        for (document in documents) {
            val post = document.toObject(PostData::class.java)
            newPosts.add(post)
        }
        val sortedPosits = newPosts.sortedByDescending { it.time }
        postsState.value = sortedPosits
    }

    // todo create interactor for this
    private fun onLogout() {
        auth.signOut()
        isSignedInState.value = false
        userState.value = null
        notificationState.value = OneTimeEvent("Logout")
    }
}