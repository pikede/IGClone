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

class GetPersonalizedFeed @Inject constructor(
    private val db: FirebaseFirestore,
    private val dispatcher: CoroutineDispatchers,
) : Interactor<List<String>, List<PostData>>() {

    override suspend fun doWork(param: List<String>): List<PostData> {
        return withContext(dispatcher.io) {
            val temp = db.collection(POSTS).whereIn("userId", param)
                .get()
                .await()
            convertPosts(temp)
        }
    }
}