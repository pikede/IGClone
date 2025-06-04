package com.example.instagram.common.util

import com.example.instagram.models.PostData
import com.google.firebase.firestore.QuerySnapshot

fun convertPosts(documents: QuerySnapshot) = buildList {
    for (document in documents) {
        val post = document.toObject(PostData::class.java)
        add(post)
    }
}.sortedByDescending { it.time }
