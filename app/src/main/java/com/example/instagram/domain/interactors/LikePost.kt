package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.POSTS
import com.example.instagram.domain.core_domain.Interactor
import com.example.instagram.domain.network.CoroutineDispatchers
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LikePost @Inject constructor(
    private val db: FirebaseFirestore,
    private val dispatcher: CoroutineDispatchers,
) : Interactor<LikePost.Params, Unit>() {
    data class Params(
        val postId: String,
        val likes: List<String>,
    )

    override suspend fun doWork(params: Params) {
        withContext(dispatcher.io) {
            db.collection(POSTS).document(params.postId).update("likes", params.likes)
                .await()
        }
    }
}