package com.resthalflab.resthalfapp.core.network

import com.resthalflab.resthalfapp.core.domain.Logger
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.logging.Logger as KtorLogger

/** Koin qualifier for the bare client used by auth endpoints (login / refresh). */
const val AUTH_HTTP_CLIENT = "authHttpClient"

class HttpClientFactory(
    private val config: NetworkConfig,
    private val logger: Logger,
) {

    /**
     * Bare client with no Bearer [Auth] plugin. Used for login and token refresh so those
     * requests never trigger the refresh flow themselves (which would recurse on a 401).
     */
    fun createAuthClient(): HttpClient = build { }

    /** Authenticated client used by all feature APIs. Attaches and refreshes Bearer tokens. */
    fun createApiClient(tokenProvider: TokenProvider): HttpClient = build {
        install(Auth) {
            bearer {
                loadTokens {
                    tokenProvider.load()?.let { BearerTokens(it.access, it.refresh) }
                }
                refreshTokens {
                    val refreshToken = oldTokens?.refreshToken ?: return@refreshTokens null
                    tokenProvider.refresh(refreshToken)?.let { BearerTokens(it.access, it.refresh) }
                }
            }
        }
    }

    private fun build(extra: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient {
        expectSuccess = true

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                }
            )
        }

        install(HttpTimeout) {
            connectTimeoutMillis = config.connectTimeoutMs
            requestTimeoutMillis = config.requestTimeoutMs
        }

        if (config.logRequests) {
            val appLogger = logger
            install(Logging) {
                level = LogLevel.INFO
                this.logger = object : KtorLogger {
                    override fun log(message: String) {
                        appLogger.info("Http", message)
                    }
                }
            }
        }

        defaultRequest {
            url(config.baseUrl)
            header(HttpHeaders.Accept, ContentType.Application.Json.toString())
        }

        extra()
    }
}
