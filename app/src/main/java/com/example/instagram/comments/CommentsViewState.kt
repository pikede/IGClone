package com.example.instagram.comments

import com.example.instagram.common.extensions.ViewEventSink
import com.example.instagram.models.CommentData

internal data class CommentsViewState(
    val inProgress: Boolean = false,
    val error: Throwable? = null,
    val comments: List<CommentData> = emptyList(),
    val eventSink: ViewEventSink<CommentsViewEvent> = {}
) {
    companion object {
        val EMPTY = CommentsViewState()
    }

    fun createComment(postId: String, commentText: String) {
        eventSink(CommentsViewEvent.CreateComment(postId, commentText))
    }
}

internal sealed interface CommentsViewEvent {
    data class CreateComment(val postId: String, val text: String) : CommentsViewEvent
}
