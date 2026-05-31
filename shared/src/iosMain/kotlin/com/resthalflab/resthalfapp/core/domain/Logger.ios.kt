package com.resthalflab.resthalfapp.core.domain

import platform.Foundation.NSLog

private class IosLogger : Logger {
    private fun log(level: String, tag: String, message: String, throwable: Throwable?) {
        val suffix = throwable?.let { " | ${it::class.simpleName}: ${it.message}" } ?: ""
        NSLog("[$level] $tag: $message$suffix")
    }

    override fun debug(tag: String, message: String, throwable: Throwable?) = log("DEBUG", tag, message, throwable)
    override fun info(tag: String, message: String, throwable: Throwable?) = log("INFO", tag, message, throwable)
    override fun warn(tag: String, message: String, throwable: Throwable?) = log("WARN", tag, message, throwable)
    override fun error(tag: String, message: String, throwable: Throwable?) = log("ERROR", tag, message, throwable)
}

actual fun platformLogger(): Logger = IosLogger()
