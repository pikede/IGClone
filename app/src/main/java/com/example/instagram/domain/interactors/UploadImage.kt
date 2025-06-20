package com.example.instagram.domain.interactors

import android.net.Uri
import com.example.instagram.domain.core_domain.Interactor
import com.example.instagram.domain.network.CoroutineDispatchers
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class UploadImage @Inject constructor(
    private val storage: FirebaseStorage,
    private val dispatchers: CoroutineDispatchers,
) : Interactor<Uri, Uri?>() {
    override suspend fun doWork(params: Uri): Uri? {
        return withContext(dispatchers.io) {
            val storageRef = storage.reference
            val uuid = UUID.randomUUID()
            val imageRef = storageRef.child("images/$uuid")
            val uploadTask = imageRef.putFile(params).await()
            uploadTask.metadata?.reference?.downloadUrl?.await()
        }
    }
}