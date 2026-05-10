package br.com.pokedex.core.observability

import timber.log.Timber

object AppLogger {
    fun init(isDebug: Boolean) {
        if (isDebug) Timber.plant(Timber.DebugTree())
    }

    fun d(message: String, vararg args: Any?) = Timber.d(message, *args)
    fun e(throwable: Throwable, message: String, vararg args: Any?) = Timber.e(throwable, message, *args)
    fun w(message: String, vararg args: Any?) = Timber.w(message, *args)
    fun i(message: String, vararg args: Any?) = Timber.i(message, *args)
}
