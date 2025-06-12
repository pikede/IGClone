package com.example.instagram.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.common.extensions.OneTimeEvent
import com.example.instagram.common.extensions.ViewEventSinkFlow
import com.example.instagram.coroutineExtensions.combine
import com.example.instagram.coroutineExtensions.stateInDefault
import com.example.instagram.domain.interactors.CreateOrUpdateProfile
import com.example.instagram.domain.interactors.GetUser
import com.example.instagram.domain.interactors.GetUserId
import com.example.instagram.domain.interactors.SignOut
import com.example.instagram.domain.interactors.UpdatePostUserImage
import com.example.instagram.domain.interactors.UploadImage
import com.example.instagram.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ProfileViewModel @Inject constructor(
    private val createOrUpdateProfile: CreateOrUpdateProfile,
    private val getUser: GetUser,
    private val signOut: SignOut,
    private val uploadImage: UploadImage,
    private val updatePostUserImage: UpdatePostUserImage,
    private val getUserId: GetUserId,
) : ViewModel() {
    private val default = ProfileViewState.Companion.Empty
    private val inProgressState = MutableStateFlow(default.inProgress)
    private val userState = MutableStateFlow(default.user)
    private val notificationState = MutableStateFlow(default.notification)
    private val errorState = MutableStateFlow(default.error)

    val state = combine(
        inProgressState,
        userState,
        notificationState,
        errorState,
        eventSink(),
        ::ProfileViewState
    ).stateInDefault(viewModelScope, default)

    init {
        getUserData()
    }

    private fun getUserData() = viewModelScope.launch {
        inProgressState.value = true
        getUser.getResult()
            .onSuccess { userState.value = it }
            .onFailure { errorState.value = it }
        inProgressState.value = false
    }

    private fun eventSink(): ViewEventSinkFlow<ProfileScreenEvent> = flowOf { event ->
        when (event) {
            ProfileScreenEvent.ConsumeError -> errorState.value = null
            is ProfileScreenEvent.UpdateBio -> userState.value =
                userState.value?.copy(bio = event.newBio)

            is ProfileScreenEvent.UpdateName -> userState.value =
                userState.value?.copy(name = event.newName)

            is ProfileScreenEvent.UpdateUserName -> userState.value =
                userState.value?.copy(userName = event.newUserName)

            ProfileScreenEvent.Save -> createOrUpdateProfile(
                name = userState.value?.name,
                username = userState.value?.userName,
                bio = userState.value?.bio,
            )

            is ProfileScreenEvent.UpdateProfileImageUrl -> uploadProfileImage(event.uri)
            ProfileScreenEvent.Logout -> onLogout()
        }
    }

    private fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        imageUrl: String? = null,
    ) = viewModelScope.launch {
        val uid = getUserId.execute()
        val userData = User(
            userId = uid,
            name = name ?: userState.value?.name,
            userName = username ?: userState.value?.userName,
            bio = bio ?: userState.value?.bio,
            imageUrl = imageUrl ?: userState.value?.imageUrl,
            following = userState.value?.following
        )
        createOrUpdateProfile.getResult(userData)
            .onSuccess { userState.value = it }
            .onFailure { errorState.value = it }
    }

    private fun uploadProfileImage(uri: Uri) = viewModelScope.launch {
        uploadImage(
            uri = uri,
            onSuccess = {
                createOrUpdateProfile(imageUrl = it.toString())
            })
    }

    private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) = viewModelScope.launch {
        inProgressState.value = true
        uploadImage.getResult(uri)
            .onSuccess { imageUri ->
                if (imageUri != null) {
                    onSuccess(imageUri)
                    updatePostUserImageData(imageUri.toString())
                } else {
                    errorState.value = Throwable("Image uploaded but not found")
                }
            }.onFailure { errorState.value = it }
        inProgressState.value = false
    }

    private suspend fun updatePostUserImageData(imageUrl: String) {
        updatePostUserImage.getResult(imageUrl)
            .onFailure {
                errorState.value = Throwable("Error: username unavailable. Unable to refresh posts")
            }
    }

    private fun onLogout() = viewModelScope.launch {
        signOut.execute()
        userState.value = null
        notificationState.value = OneTimeEvent("Logout")
    }
}

