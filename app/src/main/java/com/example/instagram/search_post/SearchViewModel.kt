package com.example.instagram.search_post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.common.extensions.ViewEventSinkFlow
import com.example.instagram.coroutineExtensions.stateInDefault
import com.example.instagram.domain.interactors.SearchPosts
import com.example.instagram.search_post.SearchPostEvent.ConsumeError
import com.example.instagram.search_post.SearchPostEvent.Search
import com.example.instagram.search_post.SearchPostEvent.UpdateSearchTerm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SearchPostsViewModel @Inject constructor(
    private val searchPosts: SearchPosts,
) : ViewModel() {
    private val default = SearchPostState.Empty
    private val inProgressState = MutableStateFlow(default.inProgress)
    private val searchTermState = MutableStateFlow(default.searchTerm)
    private val searchedPostsState = MutableStateFlow(default.searchedPosts)
    private val errorState = MutableStateFlow(default.error)
    val state = combine(
        inProgressState,
        searchTermState,
        searchedPostsState,
        errorState,
        eventSink(),
        ::SearchPostState
    ).stateInDefault(viewModelScope, default)

    private fun eventSink(): ViewEventSinkFlow<SearchPostEvent> = flowOf { event ->
        when (event) {
            is UpdateSearchTerm -> updateSearch(event.newSearchTerm)
            is ConsumeError -> errorState.value = null
            is Search -> onSearch()
        }
    }

    private fun updateSearch(newSearchTerm: String) {
        searchTermState.value = newSearchTerm
    }

    private fun onSearch() = viewModelScope.launch {
        inProgressState.value = true
        searchPosts.getResult(searchTermState.value)
            .onSuccess { searchedPostsState.value = it }
            .onFailure { errorState.value = it }
        inProgressState.value = false
    }
}