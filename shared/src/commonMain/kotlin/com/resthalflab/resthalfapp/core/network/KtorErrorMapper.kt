package com.resthalflab.resthalfapp.core.network

import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

fun Throwable.toAppError(): AppError = when (this) {
    is ClientRequestException -> {
        val code = response.status.value
        if (code == 401) AppError.Auth.SessionExpired
        else AppError.Network.Server(code, response.status.description, this)
    }
    is ServerResponseException -> AppError.Network.Server(response.status.value, response.status.description, this)
    is ResponseException -> AppError.Network.Server(response.status.value, response.status.description, this)
    is HttpRequestTimeoutException,
    is ConnectTimeoutException,
    is SocketTimeoutException -> AppError.Network.Timeout(this)
    is SerializationException -> AppError.Network.Serialization(this)
    else -> AppError.Network.NoConnection(this)
}

suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (ce: CancellationException) {
    throw ce
} catch (t: Throwable) {
    AppResult.Failure(t.toAppError())
}
