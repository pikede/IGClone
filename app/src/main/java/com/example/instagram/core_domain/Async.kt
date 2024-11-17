package com.example.instagram.core_domain

sealed class Async<out T>(val complete: Boolean, val shouldLoad: Boolean) {
    object Uninitialized : Async<Nothing>(complete = false, shouldLoad = true), Incomplete

    class Loading<out T> : Async<T>(complete = false, shouldLoad = false), Incomplete {
        override fun equals(other: Any?) = other is Loading<*>
        override fun hashCode() = "Loading".hashCode()
    }

    class Success<out T>(private val value: T) : Async<T>(complete = true, shouldLoad = false) {
        override operator fun invoke(): T = value
        override fun equals(other: Any?) = other is Success<*> && value == other.value
        override fun hashCode() = "Success".hashCode() + value.hashCode()
    }

    data class Refreshing<out T>(val value: T?) : Async<T>(complete = true, shouldLoad = false) {
        override operator fun invoke(): T? = value
    }

    data class Failed<out T>(val error: Throwable) : Async<T>(complete = true, shouldLoad = true)

    val isLoading get() = this is Loading
    val isRefreshing get() = this is Refreshing
    val isLoadingOrRefreshing get() = this is Loading || this is Refreshing
    val isSuccess get() = this is Success
    val isFailed get() = this is Failed
    val isUninitialized get() = this is Uninitialized

    open operator fun invoke(): T? = null
    interface Incomplete
}