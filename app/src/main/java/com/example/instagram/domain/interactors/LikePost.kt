package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.POSTS
import com.example.instagram.domain.core_domain.Interactor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LikePost @Inject constructor(
    private val db: FirebaseFirestore,
) : Interactor<LikePost.Params, Unit>() {
    data class Params(
        val postId: String,
        val likes: List<String>,
    )

    override suspend fun doWork(params: Params) {
        db.collection(POSTS).document(params.postId).update("likes", params.likes)
            .await()
    }
}