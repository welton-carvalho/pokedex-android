package br.com.pokedex.core.common.error

import br.com.pokedex.core.common.result.DomainError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorHandlerTest {

    @Test
    fun `SocketTimeoutException maps to Timeout`() {
        val result = ErrorHandler.handle(SocketTimeoutException())
        assertEquals(DomainError.Timeout, result)
    }

    @Test
    fun `UnknownHostException maps to Network`() {
        val result = ErrorHandler.handle(UnknownHostException())
        assertEquals(DomainError.Network, result)
    }

    @Test
    fun `IOException maps to Network`() {
        val result = ErrorHandler.handle(IOException())
        assertEquals(DomainError.Network, result)
    }

    @Test
    fun `unknown exception maps to Unknown`() {
        val result = ErrorHandler.handle(RuntimeException("unexpected"))
        assertEquals(DomainError.Unknown, result)
    }

    @Test
    fun `IllegalStateException maps to Unknown`() {
        val result = ErrorHandler.handle(IllegalStateException())
        assertEquals(DomainError.Unknown, result)
    }
}
