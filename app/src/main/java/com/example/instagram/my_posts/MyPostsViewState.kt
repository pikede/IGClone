package com.example.instagram.my_posts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.instagram.common.extensions.OneTimeEvent
import com.example.instagram.common.extensions.ViewEventSink
import com.example.instagram.core_data.Fakes
import com.example.instagram.entities.User
import com.example.instagram.new_post.PostData

@Immutable
data class MyPostsViewState(
    val inProgress: Boolean = false,
    val user: User? = null,
    val notification: OneTimeEvent<String>? = null,
    val error: Throwable? = null,
    val refreshPostsProgress: Boolean = false,
    val posts: List<PostData> = emptyList(),
    val isSignedIn: Boolean = true,
    val eventSink: ViewEventSink<MyPostsScreenEvent> = {},
) {
    companion object {
        val Empty = MyPostsViewState()

        @Composable
        fun preview(): MyPostsViewState {
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
                    MyPostsScreenEvent.ConsumeError -> state = state.copy(error = null)
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

sealed interface MyPostsScreenEvent {
    object ConsumeError : MyPostsScreenEvent
}