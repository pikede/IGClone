package com.example.instagram.single_post

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.instagram.common.extensions.OneTimeEvent
import com.example.instagram.common.extensions.ViewEventSink
import com.example.instagram.core_data.Fakes
import com.example.instagram.domain.ig_domain.PostData
import com.example.instagram.entities.User

@Immutable
data class SinglePostViewState(
    val inProgress: Boolean = false,
    val user: User? = null,
    val notification: OneTimeEvent<String>? = null,
    val error: Throwable? = null,
    val refreshPostsProgress: Boolean = false,
    val posts: List<PostData> = emptyList(),
    val isSignedIn: Boolean = true,
    val eventSink: ViewEventSink<SinglePostScreenEvent> = {},
) {
    companion object {
        val Empty = SinglePostViewState()

        @Composable
        fun preview(): SinglePostViewState {
            var state by remember {
                mutableStateOf(
                    Empty.copy(
                        inProgress = false,
                        user = Fakes.User,
                    )
                )
            }

            state = state.copy(eventSink = { event ->
                when (event) {
                    SinglePostScreenEvent.ConsumeError -> state = state.copy(error = null)
                }
            })
            return state
        }
    }

    fun getUserName() = user?.userName?.let { "@$it" }.orEmpty()
}

// todo refactor to use list of post data
data class PostRow(
    var post1: PostData? = null,
    var post2: PostData? = null,
    var post3: PostData? = null,
) {
    fun isFull() = post1 != null && post2 != null && post3 != null
    fun addPost(post: PostData) {
        when {
            post1 == null -> post1 = post
            post2 == null -> post2 = post
            post3 == null -> post3 = post
        }
    }
}

sealed interface SinglePostScreenEvent {
    object ConsumeError : SinglePostScreenEvent
}