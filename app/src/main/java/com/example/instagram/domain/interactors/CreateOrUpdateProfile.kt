package com.example.instagram.domain.interactors

import android.util.Log
import com.example.instagram.common.util.Constants.USERS
import com.example.instagram.domain.core_domain.Interactor
import com.example.instagram.models.User
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class CreateOrUpdateProfile @Inject constructor(
    private val db: FirebaseFirestore,
    private val getUserId: GetUserId
) : Interactor<CreateOrUpdateProfile.Params, Unit>() {
    data class Params(
        val user: User,
        val onSuccess: (User) -> Unit,
        val onError: (Throwable) -> Unit,
        val onUpdate: () -> Unit = {}
    )

    override suspend fun doWork(params: Params) {
        val uid = getUserId.execute()
        val userData = params.user.copy(userId = uid)

        db.collection(USERS).document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    document.reference.update(userData.toMap())
                        .addOnSuccessListener { params.onSuccess(userData) }
                        .addOnFailureListener { params.onError(it) }
                } else {
                    // updates user then, calls on update
                    db.collection(USERS).document(uid).set(userData)
                    params.onUpdate()
                }
            }.addOnFailureListener {
                params.onError(it)
                Log.e("*** Failed to createOrUpdateProfile", it.localizedMessage.orEmpty())
            }
    }
}