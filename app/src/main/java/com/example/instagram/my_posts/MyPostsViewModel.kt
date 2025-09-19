package com.example.instagram.my_posts

import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.common.extensions.ViewEventSinkFlow
import com.example.instagram.coroutineExtensions.combine
import com.example.instagram.coroutineExtensions.stateInDefault
import com.example.instagram.domain.interactors.GetFollowers
import com.example.instagram.domain.interactors.GetUser
import com.example.instagram.domain.interactors.GetUserPosts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MyPostsViewModel @Inject constructor(
    private val getUser: GetUser,
    private val getUserPosts: GetUserPosts,
    private val getFollowers: GetFollowers,
) : ViewModel() {
    private val default = MyPostsViewState.Companion.Empty
    private val inProgressState = MutableStateFlow(default.inProgress)
    private val userState = MutableStateFlow(default.user)
    private val errorState = MutableStateFlow(default.error)
    private val refreshPostsProgressState = MutableStateFlow(default.refreshPostsProgress)
    private val postsState = MutableStateFlow(default.posts)
    val followers = mutableIntStateOf(0)

    val state = combine(
        inProgressState,
        userState,
        errorState,
        refreshPostsProgressState,
        postsState,
        eventSink(),
        ::MyPostsViewState
    ).stateInDefault(viewModelScope, default)

    init {
        getUserData()
    }

    internal fun getUserData() = viewModelScope.launch {
        inProgressState.value = true
        getUser.getResult()
            .onSuccess {
                userState.value = it
                refreshPosts()
                getFollowers()
            }.onFailure {
                errorState.value = it
            }
        inProgressState.value = false
    }

    // wrapper for refreshing from view
    internal fun onRefresh() = viewModelScope.launch { refreshPosts() }

    private fun eventSink(): ViewEventSinkFlow<MyPostsScreenEvent> = flowOf { event ->
        when (event) {
            MyPostsScreenEvent.ConsumeError -> errorState.value = null
        }
    }
    private suspend fun refreshPosts() {
        refreshPostsProgressState.value = true
        getUserPosts.getResult()
            .onSuccess { sortedPosts -> postsState.value = sortedPosts }
            .onFailure {
                errorState.value = Throwable("Error: username unavailable. Unable to refresh posts")
            }
        refreshPostsProgressState.value = false
    }

    private suspend fun getFollowers() {
        followers.intValue = getFollowers.execute()
    }
}