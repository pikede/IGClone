package com.example.instagram.models

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.android.parcel.Parcelize

@Parcelize
@Immutable
data class User(
    val userId: String? = null,
    var name: String? = null,
    var userName: String? = null,
    var imageUrl: String? = null,
    var bio: String? = null,
    var following: List<String>? = null,
    var followers: List<String>? = null,
    var posts: List<String>? = null,
) : Parcelable {
    fun toMap() = mapOf(
        "userId" to userId,
        "name" to name,
        "userName" to userName,
        "imageUrl" to imageUrl,
        "bio" to bio,
        "following" to following,
        "followers" to followers,
        "posts" to posts
    )
}

