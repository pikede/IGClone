package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.POSTS
import com.example.instagram.common.util.convertPosts
import com.example.instagram.domain.core_domain.Interactor
import com.example.instagram.models.PostData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GetGeneralFeed @Inject constructor(private val db: FirebaseFirestore) :
    Interactor<Long, List<PostData>>() {

    override suspend fun doWork(param: Long): List<PostData> {
        val temp = db.collection(POSTS)
            .whereGreaterThan("time", param).get()
            .await()
        return convertPosts(temp)
    }
}