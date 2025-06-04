package com.example.instagram.new_post

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
import com.example.instagram.domain.interactors.UploadImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class NewPostViewModel @Inject constructor(
    private val createPost: CreatePost,
    private val uploadImage: UploadImage,
) : ViewModel() {
    private val default = NewPostViewState.Companion.Empty
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
        ::NewPostViewState
    ).stateInDefault(viewModelScope, default)

    private fun eventSink(): ViewEventSinkFlow<NewPostScreenEvent> = flowOf { event ->
        when (event) {
            is NewPostScreenEvent.UpdateDescription -> descriptionState.value = event.newDescription
            is NewPostScreenEvent.Post -> onPost(event.imageUri.toUri(), event.onPostSuccess)
            NewPostScreenEvent.ConsumeError -> errorState.value = null
        }
    }

    private fun onPost(uri: Uri, onPostSuccess: () -> Unit) = viewModelScope.launch {
        uploadImage(uri) {
            onCreatePost(it, onPostSuccess)
        }
    }

    private suspend fun onCreatePost(imageUri: Uri, onPostSuccess: () -> Unit) {
        inProgressState.value = true
        createPost.getResult(CreatePost.Params(imageUri.toString(), descriptionState.value))
            .onSuccess {
                notificationState.value = OneTimeEvent("Post successfully created")
                onPostSuccess.invoke()
            }.onFailure {
            errorState.value = it
            notificationState.value = OneTimeEvent("Unable to create post")
        }
    }

    private suspend fun uploadImage(uri: Uri, onImageUploaded: suspend (Uri) -> Unit) {
        inProgressState.value = true
        uploadImage.getResult(uri).onSuccess { uploadedImageUri ->
            if (uploadedImageUri != null) {
                onImageUploaded(uploadedImageUri)
            } else {
                Log.e(
                    NewPostViewModel::class.java.name,
                    "Image upload failed image is $uploadedImageUri"
                )
                errorState.value = Throwable("Image upload to firebase failed")
            }
        }.onFailure {
            errorState.value = it
            inProgressState.value = false
        }
    }
}