package com.example.instagram.single_post

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.instagram.DestinationScreen
import com.example.instagram.common.extensions.OneTimeEvent
import com.example.instagram.common.extensions.ViewEventSinkFlow
import com.example.instagram.common.util.Constants.POSTS
import com.example.instagram.common.util.Constants.POST_ID
import com.example.instagram.coroutineExtensions.combine
import com.example.instagram.coroutineExtensions.stateInDefault
import com.example.instagram.domain.interactors.GetComments
import com.example.instagram.domain.interactors.GetUser
import com.example.instagram.domain.interactors.SignOut
import com.example.instagram.domain.interactors.UpdateFollowers
import com.example.instagram.models.CommentData
import com.example.instagram.models.PostData
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SinglePostViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val db: FirebaseFirestore,
    private val getUser: GetUser,
    private val signOut: SignOut,
    private val updateFollowers: UpdateFollowers,
    private val getComments: GetComments,
) : ViewModel() {
    private val postId = savedStateHandle.toRoute<DestinationScreen.SinglePost>().postId.orEmpty()

    private val default = SinglePostViewState.Companion.Empty
    private val inProgressState = MutableStateFlow(default.inProgress)
    private val userState = MutableStateFlow(default.user)
    val notificationState = MutableStateFlow(default.notification)
    private val errorState = MutableStateFlow(default.error)
    private val refreshPostsProgressState = MutableStateFlow(default.refreshPostsProgress)
    private val postsState = MutableStateFlow(default.postData)
    private val isSignedInState = MutableStateFlow(default.isSignedIn)
    val commentsState = mutableStateOf<List<CommentData>>(listOf())
    private val commentsProgress = mutableStateOf(false)

    val state = combine(
        inProgressState,
        userState,
        notificationState,
        errorState,
        refreshPostsProgressState,
        postsState,
        isSignedInState,
        eventSink(),
        ::SinglePostViewState
    ).stateInDefault(viewModelScope, default)

    init {
        getUserData()
    }

    private fun getUserData() = viewModelScope.launch {
        inProgressState.value = true
        getUser.getResult()
            .onSuccess {
                isSignedInState.value = true
                userState.value = it
                getPostData()
                inProgressState.value = false

            }.onFailure {
                errorState.value = it
                onLogout()
            }
    }

    private fun getPostData() {
        if (postId.isEmpty()) {
            errorState.value = Throwable("Unable to load post")
            Log.e("*** Unable to load post", "$postId is empty")
            return
        }
        inProgressState.value = true
        db.collection(POSTS).whereEqualTo(POST_ID, postId).get()
            .addOnSuccessListener { postDocument ->
                postsState.value = postDocument.first().toObject(PostData::class.java)
                inProgressState.value = false
            }
            .addOnFailureListener {
                errorState.value = Throwable("Unable to load post", it)
                inProgressState.value = false
            }
    }

    private fun eventSink(): ViewEventSinkFlow<SinglePostScreenEvent> = flowOf { event ->
        when (event) {
            SinglePostScreenEvent.ConsumeError -> errorState.value = null
            is SinglePostScreenEvent.OnFollow -> onFollowClick(event.userId)
        }
    }

    fun onFollowClick(userId: String) = viewModelScope.launch {
        val following = userState.value?.following.orEmpty().toMutableList()
        when {
            userId in following -> following.remove(userId)
            else -> following += userId
        }
        updateFollowers.getResult(following)
            .onSuccess { getUserData() }
    }

    private fun onLogout() = viewModelScope.launch {
        signOut.execute()
        isSignedInState.value = false
        userState.value = null
        notificationState.value = OneTimeEvent("Logout")
    }

    suspend fun getComments() {
        commentsProgress.value = true
        getComments.getResult(postId)
            .onSuccess {
                commentsState.value = it
            }
            .onFailure {
                errorState.value = Throwable("Cannot retrieve comments", it)
            }
        commentsProgress.value = false
    }
}