package com.example.instagram.domain.interactors

import com.example.instagram.domain.core_domain.Interactor
import com.example.instagram.domain.network.CoroutineDispatchers
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateUser @Inject constructor(
    private val auth: FirebaseAuth,
    private val dispatcher: CoroutineDispatchers,
) : Interactor<CreateUser.Params, Unit>() {

    data class Params(val email: String, val password: String)

    override suspend fun doWork(params: Params) {
        withContext(dispatcher.io) {
            auth.createUserWithEmailAndPassword(params.email, params.password)
                .await()
        }
    }
}