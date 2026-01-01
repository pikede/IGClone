package com.example.instagram.new_video_post

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.instagram.common.extensions.OneTimeEvent
import com.example.instagram.common.extensions.ViewEventSink

@Immutable
internal data class NewVideoPostViewState(
    val inProgress: Boolean = false,
    val isSignedIn: Boolean = true,
    val description: String? = null,
    val notification: OneTimeEvent<String>? = null,
    val error: Throwable? = null,
    val eventSink: ViewEventSink<NewPostScreenEvent> = {},
) {
    companion object {
        val Empty = NewVideoPostViewState()

        @Composable
        fun preview(): NewVideoPostViewState {
            var state by remember {
                mutableStateOf(
                    Empty.copy(
                        inProgress = false,
                        isSignedIn = true,
                    )
                )
            }

            state = state.copy(eventSink = { event ->
                when (event) {
                    NewPostScreenEvent.ConsumeError -> state = state.copy(error = null)
                    is NewPostScreenEvent.UpdateDescription -> state =
                        state.copy(description = event.newDescription)

                    is NewPostScreenEvent.PostVideo -> {}
                }
            })
            return state
        }
    }

    fun onPost(videoUri: String, onPostSuccess: () -> Unit) {
        eventSink(NewPostScreenEvent.PostVideo(videoUri, onPostSuccess))
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
    data class PostVideo(val videoUri: String, val onPostSuccess: () -> Unit) : NewPostScreenEvent
    data class UpdateDescription(val newDescription: String) : NewPostScreenEvent
}