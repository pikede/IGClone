package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.FOLLOWING
import com.example.instagram.common.util.Constants.USERS
import com.example.instagram.domain.InvalidUserException
import com.example.instagram.domain.core_domain.InteractorWithoutParams
import com.example.instagram.domain.network.CoroutineDispatchers
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetFollowers @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val dispatchers: CoroutineDispatchers,
) : InteractorWithoutParams<Int>() {

    override suspend fun doWork(): Int {
        return withContext(dispatchers.io) {
            val uid = auth.currentUser?.uid ?: throw InvalidUserException
            return@withContext db.collection(USERS).whereArrayContains(FOLLOWING, uid)
                .get()
                .await()
                .documents.size
        }
    }
}