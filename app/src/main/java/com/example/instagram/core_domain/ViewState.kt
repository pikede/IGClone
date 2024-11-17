package com.example.instagram.core_domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

typealias ViewEventSink<T> = (T) -> Any
typealias ViewEventSinkFlow<T> = Flow<ViewEventSink<T>>

fun <T> eventSinkFlow(action: (T) -> Any): ViewEventSinkFlow<T> = flowOf {
    action(it)
}

open class OneTimeEvent<out T>(val content: T) {
    var hasBeenHandled = false
        private set

    operator fun invoke(): T? {
        return when {
            hasBeenHandled -> null
            else -> {
                hasBeenHandled = true
                content
            }
        }
    }
}