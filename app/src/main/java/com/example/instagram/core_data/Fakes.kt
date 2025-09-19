package com.example.instagram.core_data

import com.example.instagram.models.PostData
import com.example.instagram.models.User
import java.util.UUID

object Fakes {
    val User = User(
        userId = getId(),
        name = "prince",
        userName = "princeikede",
        imageUrl = "https://picsum.photos/200",
        bio = "Lorem Ipsum Dolor",
        following = getIds(),
        followers = getIds(),
        posts = getPosts()
    )

    fun getIds() = List(5) { getId() }
    fun getId() = UUID.randomUUID().toString()
    fun getPosts() = List(5) { getPostData() }
    fun postDescription() =
        "Post: Lorem Ipsum dolor sit amet, consectetur adipiscing elit, " +
                "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."

    fun getPostData() = PostData(
        postId = getId(),
        postImage = "https://picsum.photos/200",
        postDescription = postDescription(),
        likes = getIds(),
        userId = getId(),
        username = "name-${getId()}",
        userImage = "https://picsum.photos/200"
    )
}

