package br.com.pokedex.core.common.result

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val error: DomainError) : Result<Nothing>
}

inline fun <T> Result<T>.onSuccess(block: (T) -> Unit): Result<T> {
    if (this is Result.Success) block(data)
    return this
}

inline fun <T> Result<T>.onError(block: (DomainError) -> Unit): Result<T> {
    if (this is Result.Error) block(error)
    return this
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
}
