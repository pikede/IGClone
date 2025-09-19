package com.example.instagram.search_post

import android.os.Parcelable
import com.example.instagram.common.extensions.ViewEventSink
import com.example.instagram.models.PostData
import kotlinx.parcelize.Parcelize

@Parcelize
internal class SearchPostState(
    val inProgress: Boolean = false,
    val searchTerm: String = "",
    val searchedPosts: List<PostData> = listOf(),
    val error: Throwable? = null,
    val eventSink: ViewEventSink<SearchPostEvent> = {},
) : Parcelable {

    companion object {
        val Empty = SearchPostState()
    }

    fun onSearch() {
        eventSink(SearchPostEvent.Search)
    }

    fun onUpdateSearchTerm(newSearchTerm: String) {
        eventSink(SearchPostEvent.UpdateSearchTerm(newSearchTerm))
    }

    fun onConsumeError() {
        eventSink(SearchPostEvent.ConsumeError)
    }

}

sealed interface SearchPostEvent {
    data object Search : SearchPostEvent
    data class UpdateSearchTerm(val newSearchTerm: String) : SearchPostEvent
    data object ConsumeError : SearchPostEvent
}
