package com.example.instagram.coroutineExtensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

typealias ViewEventSink<T> = (T) -> Any
typealias ViewEventSinkFlow<T> = Flow<ViewEventSink<T>>

fun <T> eventSinkFlow(action: (T) -> Any): ViewEventSinkFlow<T> = flowOf {
    action(it)
}