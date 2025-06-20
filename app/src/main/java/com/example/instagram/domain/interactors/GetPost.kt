package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.POSTS
import com.example.instagram.common.util.Constants.POST_ID
import com.example.instagram.domain.core_domain.Interactor
import com.example.instagram.domain.network.CoroutineDispatchers
import com.example.instagram.models.PostData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetPost @Inject constructor(
    private val db: FirebaseFirestore,
    private val dispatcher: CoroutineDispatchers,
) : Interactor<String, PostData>() {
    override suspend fun doWork(params: String): PostData {
        return withContext(dispatcher.io) {
            db.collection(POSTS).whereEqualTo(POST_ID, params)
                .get()
                .await()
                .first()
                .toObject(PostData::class.java)
        }
    }

}