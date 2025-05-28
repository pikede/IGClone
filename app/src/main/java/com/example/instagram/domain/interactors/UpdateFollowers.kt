package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.FOLLOWING
import com.example.instagram.common.util.Constants.USERS
import com.example.instagram.domain.core_domain.Interactor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UpdateFollowers @Inject constructor(
    private val getUserId: GetUserId,
    private val db: FirebaseFirestore,
) : Interactor<List<String>, Unit>() {
    override suspend fun doWork(params: List<String>) {
        val userId = getUserId.execute()
        db.collection(USERS).document(userId).update(FOLLOWING, params).await()
    }
}