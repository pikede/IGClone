package com.example.instagram.domain.interactors

import com.example.instagram.domain.InvalidUserException
import com.example.instagram.domain.core_domain.Interactor
import com.example.instagram.domain.network.CoroutineDispatchers
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignIn @Inject constructor(
    private val auth: FirebaseAuth,
    private val dispatchers: CoroutineDispatchers,
) :
    Interactor<SignIn.Params, Unit>() {
    data class Params(val email: String, val password: String)

    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            val authResult = auth.signInWithEmailAndPassword(params.email, params.password).await()
            val currentUser = auth.currentUser
            if (authResult.user == null || currentUser == null) {
                throw InvalidUserException
                return@withContext
            }
            //  auth.currentUser?.let { getUser() } todo cache user after successful sign in and use local cache
        }
    }
}