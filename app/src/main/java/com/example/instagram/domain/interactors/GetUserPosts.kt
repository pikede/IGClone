package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.POSTS
import com.example.instagram.common.util.convertPosts
import com.example.instagram.domain.InvalidUserException
import com.example.instagram.domain.core_domain.InteractorWithoutParams
import com.example.instagram.domain.network.CoroutineDispatchers
import com.example.instagram.models.PostData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetUserPosts @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val coroutineDispatchers: CoroutineDispatchers,
) : InteractorWithoutParams<List<PostData>>() {

    override suspend fun doWork(): List<PostData> = withContext(coroutineDispatchers.io) {
        val currentUid = auth.currentUser?.uid ?: throw InvalidUserException
        val document = db.collection(POSTS)
            .whereEqualTo("userId", currentUid).get()
            .await()
        convertPosts(document)
    }
}