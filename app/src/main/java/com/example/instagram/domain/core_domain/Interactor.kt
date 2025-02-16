package com.example.instagram.domain.core_domain

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.instagram.common.extensions.Async
import com.example.instagram.common.extensions.asAsyncFailed
import com.example.instagram.common.extensions.asAsyncFlow
import com.example.instagram.common.extensions.foldToAsync
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

abstract class Interactor<in P, R> {
    /*
    * Flow that emits result of [execute] once.
    * */
    open operator fun invoke(param: P): Flow<R> = flow {
        emit(execute(param))
    }

    /*
    * Runs [prepare] and then [doWork] with given params.
    */
    suspend fun execute(params: P): R {
        prepare(params)
        return doWork(params)
    }

    /*
    * Safely run [execute] with given params using kotlin [Resul] monad.
    */
    suspend fun getResult(params: P): Result<R> = runCatching { execute(params) }
    suspend fun getAsync(params: P): Async<R> = getResult(params).foldToAsync()

    suspend fun getAsyncFlow(params: P): Flow<Async<R>> = flow {
        emit(Async.Uninitialized)
        emit(Async.Loading())
        emit(Async.Success(execute(params)))
    }.catch { e ->
        emit(e.asAsyncFailed())
    }

    /*
    * Called before [doWork], can be used to prepare or validate params.
    */
    protected open suspend fun prepare(params: P) {}

    /*
    * Called to do the actual work by the interactor.
    */
    protected abstract suspend fun doWork(params: P): R
}

/**
 * [Interactor] that has [Unit] as a parameter and has shortcut to methods with [Unit] as an argument.
 * i.e. requires no parameters/arguments.
 */
abstract class InteractorWithoutParams<R> : Interactor<Unit, R>() {
    open operator fun invoke(): Flow<R> = invoke(Unit)
    suspend fun execute(): R = execute(Unit)
    suspend fun getResult(): Result<R> = getResult(Unit)
    suspend fun getAsync(): Async<R> = getAsync(Unit)
    suspend fun getAsyncFlow(): Flow<Async<R>> = getAsyncFlow(Unit)
    protected abstract suspend fun doWork(): R
    override suspend fun doWork(params: Unit): R = doWork()
}

/**
 * Can be used to create an [Interactor] with observable results.
 * @param Model Type of the model to listen for
 * @param ResultType Model that will be emitted by the flow after each change
 */
interface DatabaseModelListener<Model, ResultType> {
    fun listen(): Flow<ResultType>
}

abstract class PagingInteractor<P : PagingInteractor.Params, T : Any> :
    SubjectInteractor<P, PagingData<T>>() {
    interface Params {
        val pagingConfig: PagingConfig
    }

    object DefaultParams : Params {
        override val pagingConfig: PagingConfig = DEFAULT_PAGING_CONFIG
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 250
        private val DEFAULT_PAGING_CONFIG = PagingConfig(
            pageSize = DEFAULT_PAGE_SIZE,
            initialLoadSize = DEFAULT_PAGE_SIZE,
            prefetchDistance = DEFAULT_PAGE_SIZE / 2, // by default, start loading the next page when we are half way through the current
            enablePlaceholders = false
        )
    }
}


@OptIn(ExperimentalCoroutinesApi::class)
abstract class SubjectInteractor<P : Any, T> {
    // Ideally this would be buffer = 0, since we use flatMapLatest below, BUT invoke is not
    // suspending. This means that we can't suspend while flatMapLatest cancels any
    // existing flows. The buffer of 1 means that we can use tryEmit() and buffer the value
    // instead, resulting in mostly the same result.
    private val paramState = MutableSharedFlow<P>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    operator fun invoke(params: P): Flow<T> {
        paramState.tryEmit(params)
        return flow
    }

    suspend fun execute(params: P): T = createObservable(params).first()

    protected abstract fun createObservable(params: P): Flow<T>

    val flow: Flow<T> = paramState
        .distinctUntilChanged()
        .flatMapLatest { createObservable(it) }
        .distinctUntilChanged()

    val asyncFlow: Flow<Async<T>> = paramState
        .distinctUntilChanged()
        .flatMapLatest { createObservable(it).asAsyncFlow() }
        .distinctUntilChanged()

    suspend fun get(): T = flow.first()
    suspend fun getOrNull(): T? = flow.firstOrNull()

    private val errorState = MutableSharedFlow<Throwable>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    protected fun onError(error: Throwable) {
        errorState.tryEmit(error)
    }

    fun errors(): Flow<Throwable> = errorState.asSharedFlow()
}