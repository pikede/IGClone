package com.example.instagram.domain.interactors

import com.example.instagram.domain.core_domain.InteractorWithoutParams
import com.example.instagram.domain.network.CoroutineDispatchers
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignOut @Inject constructor(
    private val auth: FirebaseAuth,
    private val dispatchers: CoroutineDispatchers,
) : InteractorWithoutParams<Unit>() {

    override suspend fun doWork() {
        withContext(dispatchers.io) {
            auth.signOut()
        }
    }
}