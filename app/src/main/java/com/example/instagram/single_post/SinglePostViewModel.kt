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
import com.example.instagram.common.util.Constants.COMMENTS
import com.example.instagram.common.util.Constants.FOLLOWING
import com.example.instagram.common.util.Constants.POSTS
import com.example.instagram.common.util.Constants.POST_ID
import com.example.instagram.common.util.Constants.USERS
import com.example.instagram.coroutineExtensions.combine
import com.example.instagram.coroutineExtensions.stateInDefault
import com.example.instagram.models.CommentData
import com.example.instagram.models.PostData
import com.example.instagram.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
internal class SinglePostViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
) : ViewModel() {
    private val postId = savedStateHandle.toRoute<DestinationScreen.SinglePost>().postId.orEmpty()
    val temp = savedStateHandle.getStateFlow("postId", "")

    private val default = SinglePostViewState.Companion.Empty
    private val inProgressState = MutableStateFlow(default.inProgress)
    private val userState = MutableStateFlow(default.user)
    val notificationState = MutableStateFlow(default.notification)
    private val errorState = MutableStateFlow(default.error)
    private val refreshPostsProgressState = MutableStateFlow(default.refreshPostsProgress)
    private val postsState = MutableStateFlow(default.postData)
    private val isSignedInState = MutableStateFlow(default.isSignedIn)
    val comments = mutableStateOf<List<CommentData>>(listOf())
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
        println(temp.value)// todo remove this
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
                getPostData()
                inProgressState.value = false
            }
            .addOnFailureListener {
                errorState.value = it
                onLogout()
                Log.e("*** Failed to createOrUpdateProfile", it.localizedMessage.orEmpty())
            }
    }


    private fun getPostData() {
        if (postId.isEmpty()) {
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

    fun onFollowClick(userId: String) {
        auth.currentUser?.uid?.let { currentUser ->
            val following = userState.value?.following.orEmpty().toMutableList()
            when {
                following.contains(userId) -> following.remove(userId)
                else -> following.add(userId)
            }
            db.collection(USERS).document(currentUser).update(FOLLOWING, following)
                .addOnSuccessListener {
                    getUserData(currentUser)
                }
        }
    }

    // todo create interactor for this
    private fun onLogout() {
        auth.signOut()
        isSignedInState.value = false
        userState.value = null
        notificationState.value = OneTimeEvent("Logout")
    }

    // todo move to interactor and remove duplicate from the IGviewmodel
    fun getComments() {
        commentsProgress.value = true
        db.collection(COMMENTS).whereEqualTo(POST_ID, postId).get()
            .addOnSuccessListener { documents ->
                val newComments = mutableListOf<CommentData>()
                documents.forEach { doc ->
                    val comment = doc.toObject(CommentData::class.java)
                    newComments.add(comment)
                }
                val sortedComments = newComments.sortedByDescending { it.timeStamp }
                comments.value = sortedComments
                commentsProgress.value = false
            }
            .addOnFailureListener { cause ->
                errorState.value = Throwable("Cannot retrieve comments", cause)
                commentsProgress.value = false
            }
    }
}