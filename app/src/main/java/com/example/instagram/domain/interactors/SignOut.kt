package com.example.instagram.domain.interactors

import com.example.instagram.domain.core_domain.InteractorWithoutParams
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class SignOut @Inject constructor(private val auth: FirebaseAuth) :
    InteractorWithoutParams<Unit>() {
    override suspend fun doWork() {
        auth.signOut()
    }
}