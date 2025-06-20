package com.example.instagram.domain.interactors

import com.example.instagram.domain.InvalidUserException
import com.example.instagram.domain.core_domain.InteractorWithoutParams
import com.example.instagram.domain.network.CoroutineDispatchers
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetUserId @Inject constructor(
    private val auth: FirebaseAuth,
    private val dispatchers: CoroutineDispatchers,
) :
    InteractorWithoutParams<String>() {
    // running this on dispatcher to be safe as getting currentUser might require network and lot's of processing during initial startup
    override suspend fun doWork() = withContext(dispatchers.io) {
        auth.currentUser?.uid ?: throw InvalidUserException
    }
}