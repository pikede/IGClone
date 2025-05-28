package com.example.instagram

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.auth.signup.SignupScreenEvent
import com.example.instagram.auth.signup.SignupScreenState
import com.example.instagram.common.extensions.OneTimeEvent
import com.example.instagram.common.extensions.ViewEventSinkFlow
import com.example.instagram.coroutineExtensions.combine
import com.example.instagram.coroutineExtensions.stateInDefault
import com.example.instagram.domain.InvalidUserException
import com.example.instagram.domain.UserCreationFailedException
import com.example.instagram.domain.interactors.CreateOrUpdateProfile
import com.example.instagram.domain.interactors.CreateUser
import com.example.instagram.domain.interactors.GetGeneralFeed
import com.example.instagram.domain.interactors.GetPersonalizedFeed
import com.example.instagram.domain.interactors.GetUser
import com.example.instagram.domain.interactors.LikePost
import com.example.instagram.domain.interactors.SearchPosts
import com.example.instagram.domain.interactors.SignOut
import com.example.instagram.domain.interactors.SignUp
import com.example.instagram.models.PostData
import com.example.instagram.models.User
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IgViewModel @Inject constructor(
    private val getUser: GetUser,
    private val signUp: SignUp,
    private val signOut: SignOut,
    private val createOrUpdateProfile: CreateOrUpdateProfile,
    private val createUser: CreateUser,
    private val getGeneralFeed: GetGeneralFeed,
    private val getPersonalizedFeed: GetPersonalizedFeed,
    private val likePost: LikePost,
    private val searchPosts: SearchPosts
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
    val userFeed = mutableStateOf<List<PostData>>(listOf())
    val isFeedInProgress = mutableStateOf(false)

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
        getUserData()
    }

    private fun getUserData() = viewModelScope.launch {
        inProgressState.value = true

        getUser.getResult()
            .onSuccess { user ->
                signedInState.value = true
                userState.value = user
                inProgressState.value = false
                getPersonalizedFeed()
            }
            .onFailure {
                errorState.value = it
                inProgressState.value = false
                if (it == InvalidUserException) {
                    onLogout()
                }
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

    fun onSignup() = viewModelScope.launch {
        with(state.value) {
            require(!(userName.isNullOrEmpty() or email.isNullOrEmpty() or password.isNullOrEmpty())) {
                errorState.value = Throwable("Please fill in all fields")
                return@launch
            }
        }
        inProgressState.value = true
        signUp.execute(userNameState.value.orEmpty())
            .addOnSuccessListener { documents ->
                when {
                    documents.size() > 0 -> {
                        errorState.value = Throwable("Username already exists")
                        inProgressState.value = false
                    }

                    else -> {
                        createUser()
                    }
                }
            }
            .addOnFailureListener { exception ->
                errorState.value = exception
                inProgressState.value = false
            }
    }

    private fun createUser() = viewModelScope.launch {
        createUser.execute(
            CreateUser.Params(
                email = emailState.value.orEmpty(),
                password = passwordState.value.orEmpty(),
                onCompleteListener = { task ->
                    when {
                        task.isSuccessful -> {
                            signedInState.value = true
                            createOrUpdateProfile(username = userNameState.value)
                        }

                        else -> errorState.value = task.exception ?: UserCreationFailedException
                    }
                    inProgressState.value = false
                }
            )
        )
    }

    private fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        imageUrl: String? = null,
    ) = viewModelScope.launch {
        val userData = User(
            name = name ?: userState.value?.name,
            userName = username ?: userState.value?.userName,
            bio = bio ?: userState.value?.bio,
            imageUrl = imageUrl ?: userState.value?.imageUrl,
            following = userState.value?.following
        )
        createOrUpdateProfile.execute(
            CreateOrUpdateProfile.Params(
                user = userData,
                onSuccess = { userData ->
                    userState.value = userData
                    inProgressState.value = false
                },
                onError = {
                    errorState.value = Throwable(it)
                    inProgressState.value = false
                },
                onUpdate = {
                    getUserData()
                    inProgressState.value = false
                }
            )
        )
    }

    fun searchPosts(searchTerm: String) = viewModelScope.launch {
        if (searchTerm.isNotEmpty()) {
            searchPosts.getResult(searchTerm)
                .onSuccess {
                    searchedPosts.value = it
                    searchedPostsProgress.value = false
                }
                .onFailure {
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

    // todo create interactor for this
    private suspend fun onLogout() {
        signOut.execute()
        signedInState.value = false
        userState.value = null
        notificationState.value = OneTimeEvent("Logout")
        searchedPosts.value = listOf() // TODO add this to eventual interactor, need to clear search for app restart as the search will have old results if it's not cleared
        userFeed.value = listOf()
//        comments.value = listOf() todo clear all of these on logout
    }

    private suspend fun getPersonalizedFeed() {
        val following = userState.value?.following
        if (!following.isNullOrEmpty()) {
            isFeedInProgress.value = true
            getPersonalizedFeed.getResult(following)
                .onSuccess { posts ->
                    if (posts.isNotEmpty()) {
                        userFeed.value = posts
                    } else {
                        getGeneralFeed()
                    }
                    isFeedInProgress.value = false
                }.onFailure {
                    errorState.value = it
                    notificationState.value = OneTimeEvent("Cannot fetch posts")
                    isFeedInProgress.value = false
                }
        } else {
            getGeneralFeed()
        }
    }

    private suspend fun getGeneralFeed() {
        isFeedInProgress.value = true
        val currentTime = System.currentTimeMillis()
        val difference = 24 * 10060 * 60 * 1000 // 1 day in millis
        val timeAfter = currentTime - difference
        getGeneralFeed.getResult(timeAfter)
            .onSuccess { posts ->
                userFeed.value = posts
                isFeedInProgress.value = false
            }.onFailure {
                errorState.value = it
                isFeedInProgress.value = false
            }
    }

    fun onLikePost(postData: PostData) = viewModelScope.launch {
        val postId = postData.postId
        val likes = postData.likes
        val userId = userState.value?.userId
        require(postId.isNullOrEmpty() || likes.isNullOrEmpty() || userId.isNullOrEmpty()) {
            errorState.value = Throwable("Unable to like post")
        }

        val newLikes = arrayListOf<String>()
        if (likes!!.contains(userId)) {
            newLikes.addAll(likes.filter { userId != it })
        } else {
            newLikes.addAll(likes + userId!!)
        }

        likePost.getResult(LikePost.Params(postId = postId!!, newLikes))
            .onSuccess { postData.likes = newLikes }
            .onFailure { errorState.value = Throwable("Unable to like post", it) }
    }
}


