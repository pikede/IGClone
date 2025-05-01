package com.example.instagram

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.auth.signup.SignupScreenEvent
import com.example.instagram.auth.signup.SignupScreenState
import com.example.instagram.common.extensions.OneTimeEvent
import com.example.instagram.common.extensions.ViewEventSinkFlow
import com.example.instagram.common.util.Constants.COMMENTS
import com.example.instagram.common.util.Constants.FOLLOWING
import com.example.instagram.common.util.Constants.POSTS
import com.example.instagram.common.util.Constants.POST_ID
import com.example.instagram.common.util.Constants.SEARCH_TERMS
import com.example.instagram.common.util.Constants.USERNAME
import com.example.instagram.common.util.Constants.USERS
import com.example.instagram.coroutineExtensions.combine
import com.example.instagram.coroutineExtensions.stateInDefault
import com.example.instagram.models.CommentData
import com.example.instagram.models.PostData
import com.example.instagram.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class IgViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
) : ViewModel() {
    private val default = SignupScreenState()
    private val userNameState = MutableStateFlow(default.userName)
    private val passwordState = MutableStateFlow(default.password)
    private val emailState = MutableStateFlow(default.email)
    private val signedInState = MutableStateFlow(default.signedIn)
    private val inProgressState = MutableStateFlow(default.inProgress)
    private val userState = MutableStateFlow(default.user)
    private val notificationState = MutableStateFlow(default.notification)
    private val errorState = MutableStateFlow(default.error)
    val searchedPosts = MutableStateFlow(default.searchedPosts)
    val searchedPostsProgress = MutableStateFlow(default.searchedPostsProgress)
    val postsFeed = mutableStateOf<List<PostData>>(listOf())
    val postsFeedProgress = mutableStateOf(false)
    val comments = mutableStateOf<List<CommentData>>(listOf())
    internal val commentsProgress = mutableStateOf(false)
    val followers = mutableIntStateOf(0)

    internal val state = combine(
        userNameState,
        passwordState,
        emailState,
        signedInState,
        inProgressState,
        userState,
        notificationState,
        errorState,
        searchedPosts,
        searchedPostsProgress,
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
            .get()  // TODO create a helper that gets the document from firebase in an Interactor
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                userState.value = user
                inProgressState.value = false
                refreshPosts()
                getPersonalizedFeed()
                getFollowers(user?.userId)
            }
            .addOnFailureListener {
                errorState.value = it
                Log.e("*** Failed to createOrUpdateProfile", it.localizedMessage.orEmpty())
            }
    }

    // todo move to interactor and remove duplicates
    private fun refreshPosts() {
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

    // todo move to Interactor and remove duplicate for this
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

    fun searchPosts(searchTerm: String) {
        if (searchTerm.isNotEmpty()) {
            searchedPostsProgress.value = true
            db.collection(POSTS)
                .whereArrayContains(SEARCH_TERMS, searchTerm.trim().lowercase())
                .get()
                .addOnSuccessListener {
                    convertSearchedPosts(it)
                    searchedPostsProgress.value = false
                }
                .addOnFailureListener {
                    Log.e("*** Cannot search posts", it.localizedMessage.orEmpty())
                    errorState.value = it
                    searchedPostsProgress.value = false
                }
        }
    }

    // todo move create Interactor for this
    private fun convertSearchedPosts(documents: QuerySnapshot) {
        val newPosts = mutableListOf<PostData>()
        for (document in documents) {
            val post = document.toObject(PostData::class.java)
            newPosts.add(post)
        }
        val sortedPosits = newPosts.sortedByDescending { it.time }
        searchedPosts.value = sortedPosits
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

    // todo create interactor for this
    private fun onLogout() {
        auth.signOut()
        signedInState.value = false
        userState.value = null
        notificationState.value = OneTimeEvent("Logout")
        searchedPosts.value =
            listOf() // TODO add this to eventual interactor, need to clear search for app restart as the search will have old results if it's not cleared
        postsFeed.value = listOf()
        comments.value = listOf()
    }

    private fun getPersonalizedFeed() {
        val following = userState.value?.following
        if (!following.isNullOrEmpty()) {
            postsFeedProgress.value = true
            db.collection(POSTS).whereIn("userId", following).get()
                .addOnSuccessListener { documents ->
                    convertPosts(documents = documents, outState = postsFeed)
                    if (postsFeed.value.isEmpty()) {
                        getGeneralFeed()
                    } else {
                        postsFeedProgress.value = false
                    }
                }
                .addOnFailureListener { exception ->
                    errorState.value = exception
                    notificationState.value = OneTimeEvent("Cannot fetch posts")
                    postsFeedProgress.value = false
                }

        } else {
            getGeneralFeed()
        }
    }

    private fun getGeneralFeed() {
        postsFeedProgress.value = true
        val currentTime = System.currentTimeMillis()
        val difference = 24 * 10060 * 60 * 1000 // 1 day in millis
        val timeAfter = currentTime - difference
        db.collection(POSTS)
            .whereGreaterThan("time", timeAfter).get()
            .addOnSuccessListener {
                convertPosts(documents = it, outState = postsFeed)
                postsFeedProgress.value = false
            }
            .addOnFailureListener { exc ->
                errorState.value = exc
                postsFeedProgress.value = false
            }
    }

    fun onLikePost(postData: PostData) {
        auth.currentUser?.uid?.let { userId ->
            postData.likes?.let { likes ->
                val newLikes = arrayListOf<String>()
                if (likes.contains(userId)) {
                    newLikes.addAll(likes.filter { userId != it })
                } else {
                    newLikes.addAll(likes)
                    newLikes.add(userId)
                }
                postData.postId?.let { postId ->
                    db.collection(POSTS).document(postId).update("likes", newLikes)
                        .addOnSuccessListener {
                            postData.likes = newLikes
                        }.addOnFailureListener {
                            errorState.value =
                                Throwable("Unable to like post", it)
                        }
                }
            }
        }
    }

    fun createComment(postId: String, text: String) {
        userState.value?.userName?.let { userName ->
            val commentId = UUID.randomUUID().toString()
            val comment = CommentData(
                commentId = commentId,
                postId = postId,
                userName = userName,
                text = text,
                timeStamp = System.currentTimeMillis()
            )
            db.collection(COMMENTS).document(commentId).set(comment)
                .addOnSuccessListener { getComments(postId) }
                .addOnFailureListener {
                    errorState.value = Throwable("Cannot Create Comment", it)
                }
        }
    }

    fun getComments(postId: String) {
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

    private fun getFollowers(uid: String?) {
        db.collection(USERS).whereArrayContains(FOLLOWING, uid.orEmpty()).get()
            .addOnSuccessListener { document ->
                followers.value = document.size()
            }
    }
}


