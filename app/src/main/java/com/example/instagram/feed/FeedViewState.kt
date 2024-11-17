package com.example.instagram.feed

import androidx.compose.runtime.Immutable

@Immutable
data class FeedViewState(val empty: String = "empty", val loading: Boolean = false)
