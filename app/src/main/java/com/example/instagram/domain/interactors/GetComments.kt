package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.COMMENTS
import com.example.instagram.common.util.Constants.POST_ID
import com.example.instagram.domain.core_domain.Interactor
import com.example.instagram.models.CommentData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GetComments @Inject constructor(private val db: FirebaseFirestore) :
    Interactor<String, List<CommentData>>() {

        override suspend fun doWork(params: String): List<CommentData> {
        val documents = db.collection(COMMENTS).whereEqualTo(POST_ID, params).get()
            .await()
            .documents
        val comments = documents.mapNotNull { it.toObject(CommentData::class.java) }
        return comments.sortedByDescending { it.timeStamp }
    }

}