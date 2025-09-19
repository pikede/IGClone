package com.example.instagram.domain.interactors

import android.net.Uri
import com.example.instagram.domain.core_domain.Interactor
import com.example.instagram.domain.network.CoroutineDispatchers
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class UploadVideo @Inject constructor(
    private val storage: FirebaseStorage,
    private val dispatchers: CoroutineDispatchers,
) : Interactor<Uri, Uri?>() {

    companion object{
        private const val VIDEOS_UPLOAD_PATH = "videos/"
    }

    override suspend fun doWork(params: Uri): Uri? {
        return withContext(dispatchers.io) {
            val storageRef = storage.reference
            val uuid = UUID.randomUUID()
            val videoRef = storageRef.child("$VIDEOS_UPLOAD_PATH/$uuid")
            val uploadTask = videoRef.putFile(params).await()
            uploadTask.metadata?.reference?.downloadUrl?.await()
        }
    }
}

/*
todo Coroutine worker for the upload??
todo service fore ground service upload the video show progress in the notification

notification with app logo

 */