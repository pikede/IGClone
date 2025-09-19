package com.example.instagram.domain.interactors

import com.example.instagram.domain.core_domain.Interactor
import com.example.instagram.domain.network.CoroutineDispatchers
import com.example.instagram.models.PostData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class CreatePost @Inject constructor(
    private val getUser: GetUser,
    private val db: FirebaseFirestore,
    private val dispatchers: CoroutineDispatchers,
) : Interactor<CreatePost.Params, Unit>() {

    data class Params(
        val imageUri: String? = null,
        val videoUri: String? = null,
        val description: String? = null,
    )

    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            val currentUser = getUser.execute()
            val postUuid = UUID.randomUUID().toString()

            val fillerWords = listOf("the", "be", "to", "is", "of", "and", "or", "a", "in", "it")
            val searchTerms = params.description.orEmpty()
                .split(" ", ".", ",", "?", "!", "#")
                .map { it.lowercase() }
                .filter { it.isNotEmpty() && it !in fillerWords }

            val post = PostData(
                postId = postUuid,
                userId = currentUser.userId,
                username = currentUser.userName,
                userImage = currentUser.imageUrl,
                postImage = params.imageUri,
                postVideo = params.videoUri,
                postDescription = params.description,
                time = System.currentTimeMillis(),
                likes = listOf(),
                searchTerms = searchTerms
            )
            db.collection("posts")
                .document(postUuid)
                .set(post)
                .await()
        }
    }
}