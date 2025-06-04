package com.example.instagram.domain.interactors

import com.example.instagram.domain.InvalidUserException
import com.example.instagram.domain.core_domain.InteractorWithoutParams
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class GetUserId @Inject constructor(private val auth: FirebaseAuth) :
    InteractorWithoutParams<String>() {
    override suspend fun doWork(): String {
        return auth.currentUser?.uid ?: throw InvalidUserException
    }
}