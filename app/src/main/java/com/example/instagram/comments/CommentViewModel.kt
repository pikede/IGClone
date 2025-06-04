package com.example.instagram.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.common.extensions.ViewEventSinkFlow
import com.example.instagram.coroutineExtensions.stateInDefault
import com.example.instagram.domain.interactors.CreateComment
import com.example.instagram.domain.interactors.GetComments
import com.example.instagram.domain.interactors.GetUser
import com.example.instagram.models.CommentData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val getUser: GetUser,
    private val getComments: GetComments,
    private val createComment: CreateComment,
) : ViewModel() {
    private val default = CommentsViewState.EMPTY
    private val errorState = MutableStateFlow(default.error)
    private val comments = MutableStateFlow<List<CommentData>>(default.comments)
    private val inProgress = MutableStateFlow(default.inProgress)

    internal val state = combine(
        inProgress,
        errorState,
        comments,
        eventSink(),
        ::CommentsViewState
    ).stateInDefault(viewModelScope, default)

    suspend fun getComments(postId: String) {
        inProgress.value = true
        getComments.getResult(postId)
            .onSuccess { sortedComments ->
                comments.value = sortedComments
            }.onFailure {
                errorState.value = Throwable("Cannot retrieve comments", it)
            }
        inProgress.value = false
    }

    private fun eventSink(): ViewEventSinkFlow<CommentsViewEvent> = flowOf { event ->
        when (event) {
            is CommentsViewEvent.CreateComment -> createComment(event.postId, event.text)
        }
    }

    private fun createComment(postId: String, text: String) = viewModelScope.launch {
        val userName = getUser.execute().userName
        userName?.let { userName ->
            val commentId = UUID.randomUUID().toString()
            val comment = CommentData(
                commentId = commentId,
                postId = postId,
                userName = userName,
                text = text,
                timeStamp = System.currentTimeMillis()
            )
            createComment.getResult(comment)
                .onSuccess { getComments(postId) }
                .onFailure { errorState.value = Throwable("Cannot Create Comment", it) }
        }
    }
}