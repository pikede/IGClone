package com.example.instagram.models

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Parcelize
@Immutable
data class PostData(
    val postId: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val userImage: String? = null,
    val postImage: String? = null,
    val postVideo: String? = null,
    val postDescription: String? = null,
    val time: Long? = null,
    var likes: List<String>? = null,
    val searchTerms: List<String>? = null,
) : Parcelable {
    companion object {
        const val VIDEO = "video"
        const val IMAGE = "video"

        fun PostData.isVideo() = this.postVideo != null
        fun PostData.isImage() = this.postImage != null
    }

    fun isVideoPost() = postVideo != null
    fun isImagePost() = postImage != null
}