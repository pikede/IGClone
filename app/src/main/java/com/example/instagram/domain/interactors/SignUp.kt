package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.USERNAME
import com.example.instagram.common.util.Constants.USERS
import com.example.instagram.domain.core_domain.Interactor
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SignUp @Inject constructor(
    private val db: FirebaseFirestore,
) : Interactor<String, QuerySnapshot>() {

    override suspend fun doWork(params: String): QuerySnapshot {
        return db.collection(USERS).whereEqualTo(USERNAME, params).get().await()
    }
}