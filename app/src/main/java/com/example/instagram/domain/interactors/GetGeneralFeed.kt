package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.POSTS
import com.example.instagram.common.util.convertPosts
import com.example.instagram.domain.core_domain.Interactor
import com.example.instagram.domain.network.CoroutineDispatchers
import com.example.instagram.models.PostData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetGeneralFeed @Inject constructor(
    private val db: FirebaseFirestore,
    private val dispatcher: CoroutineDispatchers,
) : Interactor<Long, List<PostData>>() {

    override suspend fun doWork(param: Long): List<PostData> {
        return withContext(dispatcher.io) {
            val posts = db.collection(POSTS)
                .whereGreaterThan("time", param).get()
                .await()
            convertPosts(posts)
        }
    }
}