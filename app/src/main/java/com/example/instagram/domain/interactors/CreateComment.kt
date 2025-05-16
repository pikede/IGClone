package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.COMMENTS
import com.example.instagram.domain.core_domain.Interactor
import com.example.instagram.models.CommentData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CreateComment @Inject constructor(private val db: FirebaseFirestore) :
    Interactor<CommentData, Unit>() {

    override suspend fun doWork(params: CommentData) {
        db.collection(COMMENTS).document(params.commentId.orEmpty())
            .set(params)
            .await()
    }
}