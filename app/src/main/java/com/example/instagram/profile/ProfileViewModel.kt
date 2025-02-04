package com.example.instagram.profile

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
internal class ProfileViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage,
) : ViewModel() {
    private val default = ProfileViewState.Companion.Empty
    private val inProgressState = MutableStateFlow(default.inProgress)
    private val isSignedInState = MutableStateFlow(default.isSignedIn)
    private val userState = MutableStateFlow(default.user)
    val notificationState = MutableStateFlow(default.notification)
    private val errorState = MutableStateFlow(default.error)

    val state = combine(
        inProgressState,
        isSignedInState,
        userState,
        notificationState,
        errorState,
        eventSink(),
        ::ProfileViewState
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

    private fun eventSink(): ViewEventSinkFlow<ProfileScreenEvent> = flowOf { event ->
        when (event) {
            ProfileScreenEvent.ConsumeError -> errorState.value = null
            is ProfileScreenEvent.UpdateBio -> userState.value =
                userState.value?.copy(bio = event.newBio)

            is ProfileScreenEvent.UpdateName -> userState.value =
                userState.value?.copy(name = event.newName)

            is ProfileScreenEvent.UpdateUserName -> userState.value =
                userState.value?.copy(userName = event.newUserName)

            ProfileScreenEvent.Save -> onSave()
            is ProfileScreenEvent.UpdateProfileImageUrl -> uploadProfileImage(event.uri)
            ProfileScreenEvent.Logout -> onLogout()
        }
    }

    private fun ProfileViewModel.onSave() {
        createOrUpdateProfile(
            name = userState.value?.name,
            username = userState.value?.userName,
            bio = userState.value?.bio,
//            imageUrl = userState.value?.imageUrl // commented out as no image is being set currently
        )
    }

    // TODO create interactor for this
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

    private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProgressState.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask
            .addOnSuccessListener {
                val result = it.metadata?.reference?.downloadUrl
                result?.addOnSuccessListener(onSuccess)
            }
            .addOnFailureListener {
                errorState.value = it
                inProgressState.value = false
            }
    }

    private fun uploadProfileImage(uri: Uri) {
        uploadImage(
            uri = uri,
            onSuccess = {
                createOrUpdateProfile(imageUrl = it.toString())
                updatePostUserImageData(it.toString())
            })
    }

    // todo move to interactor
    private fun updatePostUserImageData(imageUrl: String) {
        val currentuUid = auth.currentUser?.uid
        db.collection(POSTS)
            .whereEqualTo("userId", currentuUid)
            .get()
            .addOnSuccessListener {
                val posts = mutableStateOf<List<PostData>>(arrayListOf())
                convertPosts(it, posts)
                val refs = arrayListOf<DocumentReference>()
                for (post in posts.value) {
                    post.postId?.let { id ->
                        refs.add(db.collection(POSTS).document(id))
                    }
                }
                if (refs.isNotEmpty()) {
                    db.runBatch { batch ->
                        for (ref in refs) {
                            batch.update(ref, "userImage", imageUrl)
                        }
                    }.addOnSuccessListener {
                        refreshPosts()
                    }
                }
            }
    }

    // todo move to Interactor
    internal fun refreshPosts() {
        val currentUid = auth.currentUser?.uid
        currentUid?.let {
            inProgressState.value = true
            db.collection(POSTS)
                .whereEqualTo("userId", currentUid).get()
                .addOnSuccessListener { documents ->
                    convertPosts(documents)
                }.addOnFailureListener {
                    errorState.value = it
                    notificationState.value = OneTimeEvent("Cannot fetch posts")
                }
            inProgressState.value = false
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

    // todo move create Interactor for this
    private fun convertPosts(documents: QuerySnapshot) {
        /* todo duplicate
        val newPosts = mutableListOf<PostData>()
                for (document in documents) {
                    val post = document.toObject(PostData::class.java)
                    newPosts.add(post)
                }
                val sortedPosits = newPosts.sortedByDescending { it.time }
                postsState.value = sortedPosits*/
    }

    // todo create interactor for this
    private fun onLogout() {
        auth.signOut()
        isSignedInState.value = false
        userState.value = null
        notificationState.value = OneTimeEvent("Logout")
    }
}

