package com.example.instagram.new_video_post

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.common.extensions.OneTimeEvent
import com.example.instagram.common.extensions.ViewEventSinkFlow
import com.example.instagram.coroutineExtensions.combine
import com.example.instagram.coroutineExtensions.stateInDefault
import com.example.instagram.domain.interactors.CreatePost
import com.example.instagram.domain.interactors.UploadVideo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class NewVideoPostViewModel @Inject constructor(
    private val createPost: CreatePost,
    private val uploadVideo: UploadVideo,
) : ViewModel() {
    private val default = NewVideoPostViewState.Companion.Empty
    private val inProgressState = MutableStateFlow(default.inProgress)
    private val isSignedInState = MutableStateFlow(default.isSignedIn)
    private val descriptionState = MutableStateFlow(default.description)
    private val notificationState = MutableStateFlow(default.notification)
    private val errorState = MutableStateFlow(default.error)

    val state = combine(
        inProgressState,
        isSignedInState,
        descriptionState,
        notificationState,
        errorState,
        eventSink(),
        ::NewVideoPostViewState
    ).stateInDefault(viewModelScope, default)

    private fun eventSink(): ViewEventSinkFlow<NewPostScreenEvent> = flowOf { event ->
        when (event) {
            is NewPostScreenEvent.UpdateDescription -> descriptionState.value = event.newDescription
            is NewPostScreenEvent.PostVideo -> onPost(event.videoUri.toUri(), event.onPostSuccess)
            NewPostScreenEvent.ConsumeError -> errorState.value = null
        }
    }

    private fun onPost(uri: Uri, onPostSuccess: () -> Unit) = viewModelScope.launch {
        uploadVideo(uri) {
            onCreatePost(it, onPostSuccess)
        }
    }

    private suspend fun onCreatePost(videoUri: Uri, onPostSuccess: () -> Unit) {
        inProgressState.value = true
        createPost.getResult(CreatePost.Params(videoUri = videoUri.toString(), description = descriptionState.value))
            .onSuccess {
                notificationState.value = OneTimeEvent("PostVideo successfully created")
                onPostSuccess.invoke()
            }.onFailure {
                errorState.value = it
                notificationState.value = OneTimeEvent("Unable to create post")
            }
    }

    private suspend fun uploadVideo(uri: Uri, onVideoUploaded: suspend (Uri) -> Unit) {
        inProgressState.value = true
        uploadVideo.getResult(uri).onSuccess { uploadedVideoUri ->
            if (uploadedVideoUri != null) {
                onVideoUploaded(uploadedVideoUri)
            } else {
                Log.e(
                    NewVideoPostViewModel::class.java.name,
                    "Video upload failed video is $uploadedVideoUri"
                )
                errorState.value = Throwable("Video upload to firebase failed")
            }
        }.onFailure {
            errorState.value = it
            inProgressState.value = false
        }
    }
}