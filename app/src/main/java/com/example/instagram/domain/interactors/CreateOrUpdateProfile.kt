package com.example.instagram.domain.interactors

import com.example.instagram.common.util.Constants.USERS
import com.example.instagram.domain.core_domain.Interactor
import com.example.instagram.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CreateOrUpdateProfile @Inject constructor(
    private val db: FirebaseFirestore,
    private val getUserId: GetUserId,
    private val getUser: GetUser
) : Interactor<User, User>() {

    override suspend fun doWork(params: User) : User {
        val uid = getUserId.execute()
        val userData = params.copy(userId = uid)
        val document = db.collection(USERS).document(uid).get().await()

        return if (document.exists()) {
            document.reference.update(params.toMap()).await()
            userData
        } else {
            db.collection(USERS).document(uid).set(userData).await()
            getUser.execute()
        }
    }
}