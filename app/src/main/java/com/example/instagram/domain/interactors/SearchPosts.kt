package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.POSTS
import com.example.instagram.common.util.Constants.SEARCH_TERMS
import com.example.instagram.common.util.convertPosts
import com.example.instagram.domain.core_domain.Interactor
import com.example.instagram.models.PostData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SearchPosts @Inject constructor(
    private val db: FirebaseFirestore,
) : Interactor<String, List<PostData>>() {
    override suspend fun doWork(params: String): List<PostData> {
        return db.collection(POSTS).whereArrayContains(SEARCH_TERMS, params)
            .get()
            .await()
            .let { convertPosts(it) }
    }
}