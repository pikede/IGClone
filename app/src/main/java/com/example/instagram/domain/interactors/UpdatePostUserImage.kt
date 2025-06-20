package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.POSTS
import com.example.instagram.domain.core_domain.Interactor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdatePostUserImage @Inject constructor(
    private val db: FirebaseFirestore,
    private val getUserPosts: GetUserPosts,
) : Interactor<String, Unit>() {
    override suspend fun doWork(params: String) {
        return withContext(Dispatchers.IO) {
            val userPosts = getUserPosts.execute()

            if (userPosts.isEmpty()) {
                return@withContext // Nothing to update
            }

            // Create a batch
            val batch = db.batch()

            for (post in userPosts) {
                post.postId?.let { id ->
                    if (id.isNotEmpty()) {
                        val postRef = db.collection(POSTS).document(id)
                        batch.update(postRef, "userImage", params)
                    }
                }
            }

            // Commit the batch
            // Since we are already on Dispatchers.IO, this is fine.
            batch.commit().await()
        }
    }
}