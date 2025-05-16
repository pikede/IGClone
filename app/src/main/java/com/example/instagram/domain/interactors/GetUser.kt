package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.USERS
import com.example.instagram.domain.InvalidUserException
import com.example.instagram.domain.UserNotFoundException
import com.example.instagram.domain.core_domain.InteractorWithoutParams
import com.example.instagram.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GetUser @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : InteractorWithoutParams<User>() {

    override suspend fun doWork() : User {
        val uid = auth.currentUser?.uid ?: throw InvalidUserException
        return db.collection(USERS).document(uid)
            .get()
            .await()
            .toObject(User::class.java) ?: throw UserNotFoundException
    }
}