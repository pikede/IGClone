package com.example.instagram.domain.interactors

import com.example.instagram.domain.core_domain.Interactor
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CreateUser @Inject constructor(
    private val auth: FirebaseAuth,
) : Interactor<CreateUser.Params, Unit>() {

    data class Params(val email: String, val password: String)

    override suspend fun doWork(params: Params) {
        auth.createUserWithEmailAndPassword(params.email, params.password)
            .await()
    }
}