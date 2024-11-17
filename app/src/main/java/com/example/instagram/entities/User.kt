package com.example.instagram.entities

import androidx.compose.runtime.Immutable

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
) {
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

