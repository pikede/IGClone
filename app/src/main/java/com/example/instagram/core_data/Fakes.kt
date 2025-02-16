package com.example.instagram.core_data

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
    fun getPosts() = List(5) { getPost().toList().shuffled().toString() }
    fun getPost() =
        "Lorem Ipsum dolor sit amet, consectetur adipiscing elit, " +
                "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
}

