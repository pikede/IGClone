package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.USERNAME
import com.example.instagram.common.util.Constants.USERS
import com.example.instagram.domain.core_domain.Interactor
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import javax.inject.Inject

class SignUp @Inject constructor(
    private val db: FirebaseFirestore,
) : Interactor<String, Task<QuerySnapshot>>() {

    override suspend fun doWork(params: String): Task<QuerySnapshot> {
        return db.collection(USERS).whereEqualTo(USERNAME, params).get()
    }
}