package com.example.instagram.my_posts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.instagram.common.extensions.ViewEventSink
import com.example.instagram.core_data.Fakes
import com.example.instagram.models.PostData
import com.example.instagram.models.User

@Immutable
data class MyPostsViewState(
    val inProgress: Boolean = false,
    val user: User? = null,
    val error: Throwable? = null,
    val refreshPostsProgress: Boolean = false,
    val posts: List<PostData> = emptyList(),
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

sealed interface MyPostsScreenEvent {
    object ConsumeError : MyPostsScreenEvent
}