package br.com.pokedex.core.common.error

import br.com.pokedex.core.common.result.DomainError
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorHandler {
    fun handle(throwable: Throwable): DomainError = when (throwable) {
        is SocketTimeoutException -> DomainError.Timeout
        is UnknownHostException -> DomainError.Network
        is IOException -> DomainError.Network
        else -> DomainError.Unknown
    }
}
