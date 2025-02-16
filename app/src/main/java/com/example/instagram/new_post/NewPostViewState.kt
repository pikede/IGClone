package com.example.instagram.new_post

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.instagram.common.extensions.OneTimeEvent
import com.example.instagram.common.extensions.ViewEventSink
import com.example.instagram.core_data.Fakes
import com.example.instagram.models.User

@Immutable
internal data class NewPostViewState(
    val inProgress: Boolean = false,
    val isSignedIn: Boolean = true,
    val description: String? = null,
    val user: User? = null,
    val notification: OneTimeEvent<String>? = null,
    val error: Throwable? = null,
    val eventSink: ViewEventSink<NewPostScreenEvent> = {},
) {
    companion object {
        val Empty = NewPostViewState()

        @Composable
        fun preview(): NewPostViewState {
            var state by remember {
                mutableStateOf(
                    Empty.copy(
                        inProgress = false,
                        isSignedIn = true,
                        user = Fakes.User,
                    )
                )
            }

            state = state.copy(eventSink = { event ->
                when (event) {
                    NewPostScreenEvent.ConsumeError -> state = state.copy(error = null)
                    is NewPostScreenEvent.UpdateDescription -> state =
                        state.copy(description = event.newDescription)

                    is NewPostScreenEvent.Post -> {}
                }
            })
            return state
        }
    }

    fun onPost(imageUri: String, onPostSuccess: () -> Unit) {
        eventSink(NewPostScreenEvent.Post(imageUri, onPostSuccess))
    }

    fun updateDescription(newDescription: String) {
        eventSink(NewPostScreenEvent.UpdateDescription(newDescription))
    }

    fun consumeError() {
        eventSink(NewPostScreenEvent.ConsumeError)
    }
}

sealed interface NewPostScreenEvent {
    object ConsumeError : NewPostScreenEvent
    data class Post(val imageUri: String, val onPostSuccess: () -> Unit) : NewPostScreenEvent
    data class UpdateDescription(val newDescription: String) : NewPostScreenEvent
}