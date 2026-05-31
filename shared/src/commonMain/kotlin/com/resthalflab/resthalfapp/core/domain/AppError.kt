package com.resthalflab.resthalfapp.core.domain

sealed class AppError(open val message: String, open val cause: Throwable? = null) {

    sealed class Network(message: String, cause: Throwable? = null) : AppError(message, cause) {
        data class NoConnection(override val cause: Throwable? = null) : Network("No internet connection", cause)
        data class Timeout(override val cause: Throwable? = null) : Network("Request timed out", cause)
        data class Server(val statusCode: Int, override val message: String, override val cause: Throwable? = null) : Network(message, cause)
        data class Serialization(override val cause: Throwable? = null) : Network("Failed to parse response", cause)
    }

    sealed class Auth(message: String) : AppError(message) {
        data object Unauthenticated : Auth("Not signed in")
        data object SessionExpired : Auth("Session expired, please sign in again")
        data object InvalidCredentials : Auth("Email or password is incorrect")
    }

    data class Validation(val field: String, override val message: String) : AppError(message)

    data class Unknown(override val message: String = "Something went wrong", override val cause: Throwable? = null) : AppError(message, cause)
}
