package com.example.instagram.common.extensions

import com.example.instagram.coroutineExtensions.delayItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Starts with [Async.Loading], then maps results to [Async.Success] or [Async.Failed] when errors are caught.
 */
fun <T> Flow<T>.asAsyncFlow(): Flow<Async<T>> =
    map { Async.Success(it) as Async<T> }
        .onStart { emit(Async.Loading()) }
        .catch { it.asAsyncSuccess() }

fun <T> Result<T>.foldToAsync(): Async<T> = fold(
    onSuccess = { it.asAsyncSuccess() },
    onFailure = { it.asAsyncFailed() }
)

/**
 * Delays emitting [Async.Loading] for given [timeInMillis]  to skip loading state before Success or Fail
 */
fun <T> Flow<Async<T>>.delayLoading(timeInMillis: Long = 100L): Flow<Async<T>> =
    delayItem(timeInMillis, Async.Loading())

fun <T> Flow<Async<T>>.throttleLoading(timeInMillis: Long) = delayLoading(timeInMillis)

/**
 * Emits values of [Async.Success] state only
 */
fun <T> Flow<Async<T>>.filterSuccess(): Flow<T> = filterIsInstance<Async.Success<T>>().map { it() }

sealed class Async<out T>(val complete: Boolean, val shouldLoad: Boolean) {
    object Uninitialized : Async<Nothing>(complete = false, shouldLoad = true), Incomplete

    class Loading<out T> : Async<T>(complete = false, shouldLoad = false), Incomplete {
        override fun equals(other: Any?) = other is Loading<*>
        override fun hashCode() = "Loading".hashCode()
    }

    class Success<out T>(private val value: T) : Async<T>(complete = true, shouldLoad = false) {
        override operator fun invoke(): T = value
    }

    data class Refreshing<out T>(val value: T?) : Async<T>(complete = true, shouldLoad = false) {
        override operator fun invoke(): T? = value
    }

    data class Failed<out T>(val error: Throwable) : Async<T>(complete = true, shouldLoad = true)

    open operator fun invoke(): T? = null

    interface Incomplete

    val isLoading get() = this is Loading
    val isRefreshing get() = this is Refreshing
    val isLoadingOrRefreshing get() = this is Loading || this is Refreshing
    val isSuccessOrRefreshing get() = this is Success || this is Refreshing
    val isSuccess get() = this is Success
    val isFailed get() = this is Failed
    val isUninitialized get() = this is Uninitialized
    val isComplete get() = this is Success || this is Failed

    /**
     * Maps success value to [R] or returns current state
     */
    fun <R> map(transform: (T) -> R): Async<R> {
        return when (this) {
            is Success -> Success(transform(this()))
            is Loading -> Loading()
            is Failed -> Failed(error = this.error)
            is Refreshing -> Refreshing(value = this()?.let { transform(it) })
            is Uninitialized -> Uninitialized
        }
    }

    fun toRefreshing(): Async<T> = when (val value = this()) {
        null -> Loading()
        else -> Refreshing(value)
    }

    /**
     * Maps success value to [R] or returns [elseValue] if not [Success]
     */
    fun <R> mapSuccess(transform: (T) -> R, elseValue: R): R =
        map(transform).let { if (it is Success) it() else elseValue }

    /**
     * Maps success value to [transform] or false
     */
    fun mapSuccessOrFalse(transform: (T) -> Boolean): Boolean =
        map(transform).let { if (it is Success) it() else false }

}

fun <T> T.asAsyncSuccess(): Async<T> = Async.Success(this)
fun <T> Throwable.asAsyncFailed(): Async<T> = Async.Failed(this)
