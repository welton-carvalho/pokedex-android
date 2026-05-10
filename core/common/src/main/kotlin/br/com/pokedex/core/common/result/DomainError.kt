package br.com.pokedex.core.common.result

sealed interface DomainError {
    data object Network : DomainError
    data object Timeout : DomainError
    data object Unauthorized : DomainError
    data object NotFound : DomainError
    data object Unknown : DomainError
}
