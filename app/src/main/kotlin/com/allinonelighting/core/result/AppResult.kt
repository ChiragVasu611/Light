package com.allinonelighting.core.result

/**
 * Outcome of an operation that can fail in a way the user must be told about.
 *
 * No exception crosses a layer boundary: data and render implementations catch
 * platform exceptions and map them to [AppError]. See docs/TECHNICAL_ARCHITECTURE.md §6.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>

    val isSuccess: Boolean get() = this is Success

    fun getOrNull(): T? = (this as? Success)?.value
    fun errorOrNull(): AppError? = (this as? Failure)?.error
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

inline fun <T> AppResult<T>.onFailure(action: (AppError) -> Unit): AppResult<T> {
    if (this is AppResult.Failure) action(error)
    return this
}

fun success(): AppResult<Unit> = AppResult.Success(Unit)
fun failure(error: AppError): AppResult<Nothing> = AppResult.Failure(error)
