package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.COMMENTS
import com.example.instagram.common.util.Constants.POST_ID
import com.example.instagram.domain.core_domain.Interactor
import com.example.instagram.domain.network.CoroutineDispatchers
import com.example.instagram.models.CommentData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetComments @Inject constructor(
    private val db: FirebaseFirestore,
    private val dispatchers: CoroutineDispatchers,
) : Interactor<String, List<CommentData>>() {

    override suspend fun doWork(params: String): List<CommentData> {
        return withContext(dispatchers.io) {

            val documents = db.collection(COMMENTS).whereEqualTo(POST_ID, params).get()
                .await()
                .documents

            val comments = documents.mapNotNull { it.toObject(CommentData::class.java) }
            comments.sortedByDescending { it.timeStamp }
        }
    }
}