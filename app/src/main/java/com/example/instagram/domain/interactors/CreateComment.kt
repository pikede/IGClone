package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.COMMENTS
import com.example.instagram.domain.core_domain.Interactor
import com.example.instagram.domain.network.CoroutineDispatchers
import com.example.instagram.models.CommentData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateComment @Inject constructor(
    private val db: FirebaseFirestore,
    private val dispatcher: CoroutineDispatchers,
) : Interactor<CommentData, Unit>() {

    override suspend fun doWork(params: CommentData) {
        withContext(dispatcher.io) {
            db.collection(COMMENTS).document(params.commentId.orEmpty())
                .set(params)
                .await()
        }
    }
}