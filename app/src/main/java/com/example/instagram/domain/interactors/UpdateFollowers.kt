package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.FOLLOWING
import com.example.instagram.common.util.Constants.USERS
import com.example.instagram.domain.core_domain.Interactor
import com.example.instagram.domain.network.CoroutineDispatchers
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateFollowers @Inject constructor(
    private val getUserId: GetUserId,
    private val db: FirebaseFirestore,
    private val dispatchers: CoroutineDispatchers,
) : Interactor<List<String>, Unit>() {

    override suspend fun doWork(params: List<String>) {
        withContext(dispatchers.io) {
            val userId = getUserId.execute()
            db.collection(USERS).document(userId).update(FOLLOWING, params).await()
        }
    }
}