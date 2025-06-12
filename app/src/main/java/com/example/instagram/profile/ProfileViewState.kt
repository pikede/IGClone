package com.example.instagram.profile

import android.net.Uri
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
data class ProfileViewState(
    val inProgress: Boolean = false,
    val user: User? = null,
    val notification: OneTimeEvent<String>? = null,
    val error: Throwable? = null,
    val eventSink: ViewEventSink<ProfileScreenEvent> = {},
) {
    companion object {
        val Empty = ProfileViewState()

        @Composable
        fun preview(): ProfileViewState {
            var user by remember { mutableStateOf<User>(Fakes.User) }
            var error = remember { mutableStateOf<Throwable?>(null) }
            var state by remember {
                mutableStateOf(
                    Empty.copy(
                        inProgress = false,
                        user = user,
                    )
                )
            }

            state = state.copy(eventSink = { event ->
                when (event) {
                    ProfileScreenEvent.ConsumeError -> error.value = null
                    is ProfileScreenEvent.UpdateBio -> user =
                        user.copy(bio = event.newBio)

                    is ProfileScreenEvent.UpdateName -> user =
                        user.copy(name = event.newName)

                    is ProfileScreenEvent.UpdateUserName -> user =
                        user.copy(userName = event.newUserName)

                    is ProfileScreenEvent.UpdateProfileImageUrl -> user =
                        user.copy(imageUrl = event.uri.toString())

                    ProfileScreenEvent.Save -> {}
                    else -> {}
                }
            })
            return state
        }
    }

    fun getUserName() = user?.userName?.let { "@$it" }.orEmpty()

    fun updateName(newName: String) {
        eventSink(ProfileScreenEvent.UpdateName(newName))
    }

    fun updateUserName(newUserName: String) {
        eventSink(ProfileScreenEvent.UpdateUserName(newUserName))
    }

    fun updateBio(newBio: String) {
        eventSink(ProfileScreenEvent.UpdateBio(newBio))
    }

    fun onSave() {
        eventSink(ProfileScreenEvent.Save)
    }

    fun updateProfileImageUrl(uri: Uri) {
        eventSink(ProfileScreenEvent.UpdateProfileImageUrl(uri))
    }

    fun onLogout() {
        eventSink(ProfileScreenEvent.Logout)
    }
}

sealed interface ProfileScreenEvent {
    object ConsumeError : ProfileScreenEvent
    object Save : ProfileScreenEvent
    data object Logout : ProfileScreenEvent
    data class UpdateName(val newName: String) : ProfileScreenEvent
    data class UpdateUserName(val newUserName: String) : ProfileScreenEvent
    data class UpdateBio(val newBio: String) : ProfileScreenEvent
    data class UpdateProfileImageUrl(val uri: Uri) : ProfileScreenEvent
}