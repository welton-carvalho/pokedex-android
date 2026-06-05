package br.com.pokedex.core.common.result

sealed interface AsyncResult<out T> {
    data object Loading : AsyncResult<Nothing>
    data class Success<T>(val data: T) : AsyncResult<T>
    data class Error(val error: DomainError) : AsyncResult<Nothing>
}
