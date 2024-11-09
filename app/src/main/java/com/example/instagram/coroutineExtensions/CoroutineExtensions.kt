package com.example.instagram.coroutineExtensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// TODO move to separate module to be reused
fun <T> Flow<T>.stateInDefault(
    scope: CoroutineScope,
    initialValue: T,
): StateFlow<T> = stateIn(
    scope = scope,
    started = SharingStarted.WhileSubscribed(5000), // state resets after 5 seconds when no subscribers are present
    initialValue = initialValue
)