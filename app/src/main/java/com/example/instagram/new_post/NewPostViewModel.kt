package com.example.instagram.new_post

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.common.extensions.OneTimeEvent
import com.example.instagram.common.extensions.ViewEventSinkFlow
import com.example.instagram.common.util.Constants.POSTS
import com.example.instagram.common.util.Constants.USERS
import com.example.instagram.coroutineExtensions.combine
import com.example.instagram.coroutineExtensions.stateInDefault
import com.example.instagram.domain.ig_domain.PostData
import com.example.instagram.entities.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
internal class NewPostViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage,
) : ViewModel() {
    private val default = NewPostViewState.Companion.Empty
    private val inProgressState = MutableStateFlow(default.inProgress)
    private val isSignedInState = MutableStateFlow(default.isSignedIn)
    private val descriptionState = MutableStateFlow(default.description)
    private val userState = MutableStateFlow(default.user)
    private val notificationState = MutableStateFlow(default.notification)
    private val errorState = MutableStateFlow(default.error)
    val refreshPostsProgressState = MutableStateFlow(false)
    val postsState = mutableStateOf<List<PostData>>(listOf())

    val state = combine(
        inProgressState,
        isSignedInState,
        descriptionState,
        userState,
        notificationState,
        errorState,
        eventSink(),
        ::NewPostViewState
    ).stateInDefault(viewModelScope, default)

    init {
        val currentUser = auth.currentUser
        currentUser?.uid?.let { userId ->
            getUserData(userId) // todo cash getUserData after getting with interactor value
        }
    }

    private fun getUserData(uid: String) {
        inProgressState.value = true
        db.collection(USERS).document(uid)
            .get()  // TODO create a helper that gets the document from firebase in an Interactor
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                userState.value = user
                inProgressState.value = false
                isSignedInState.value = true
                // todo create interactor for this refresh
                refreshPosts() // TODO add to other getUserData functions
            }
            .addOnFailureListener {
                errorState.value = it
                isSignedInState.value = false
                Log.e("*** Failed to createOrUpdateProfile", it.localizedMessage.orEmpty())
            }
    }

    private fun eventSink(): ViewEventSinkFlow<NewPostScreenEvent> = flowOf { event ->
        when (event) {
            is NewPostScreenEvent.UpdateDescription -> descriptionState.value = event.newDescription
            is NewPostScreenEvent.Post -> onPost(event.imageUri.toUri(), event.onPostSuccess)
            NewPostScreenEvent.ConsumeError -> errorState.value = null
        }
    }

    private fun onPost(uri: Uri, onPostSuccess: () -> Unit) {
        uploadImage(uri) {
            onCreatePost(it, onPostSuccess)
        }
    }

    private fun onCreatePost(imageUri: Uri, onPostSuccess: () -> Unit) {
        inProgressState.value = true
        val currentUuid = auth.currentUser?.uid
        val currentUserName = userState.value?.userName
        val currentUserImage = userState.value?.imageUrl

        currentUuid?.let {
            val postUuid = UUID.randomUUID().toString()
            val post = PostData(
                postId = postUuid,
                userId = currentUuid,
                username = currentUserName,
                userImage = currentUserImage,
                postImage = imageUri.toString(),
                postDescription = descriptionState.value,
                time = System.currentTimeMillis(),
                likes = listOf()
            )
            db.collection(POSTS).document(postUuid).set(post)
                .addOnSuccessListener {
                    notificationState.value = OneTimeEvent("Post successfully created")
                    refreshPosts()
                    onPostSuccess.invoke()
                }
                .addOnFailureListener {
                    errorState.value = it
                    notificationState.value = OneTimeEvent("Unable to create post")
                }
            inProgressState.value = false
        } ?: run {
            invalidUserName()
        }
    }

    private fun invalidUserName() {
        Log.e(NewPostViewModel::class.java.name, "Failed to create post currentUuid is null")
        errorState.value = Throwable("Error: username is unavailable. Unable to create post")
        onLogout()
        inProgressState.value = false
    }

    // TODO extract to interactor and remove duplicate from [ProfileViewModel], interactor should use Firebase repository
    private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProgressState.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask
            .addOnSuccessListener {
                val result = it.metadata?.reference?.downloadUrl
                result?.addOnSuccessListener {
                    onSuccess(it)
                }
            }
            .addOnFailureListener {
                errorState.value = it
                inProgressState.value = false
            }
    }

    // todo create interactor for this
    private fun onLogout() {
        auth.signOut()
        isSignedInState.value = false
        userState.value = null
        notificationState.value = OneTimeEvent("Logout")
    }

    private fun refreshPosts() {
        val currentUid = auth.currentUser?.uid
        currentUid?.let {
            refreshPostsProgressState.value = true
            db.collection(POSTS)
                .whereEqualTo("userId", currentUid).get()
                .addOnSuccessListener { documents ->
                    convertPosts(documents, postsState)
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
    private fun convertPosts(documents: QuerySnapshot, outState: MutableState<List<PostData>>) {
        val newPosts = mutableListOf<PostData>()
        for (document in documents) {
            val post = document.toObject(PostData::class.java)
            newPosts.add(post)
        }
        val sortedPosits = newPosts.sortedByDescending { it.time }
        outState.value = sortedPosits
    }
}