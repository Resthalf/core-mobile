package com.resthalflab.resthalfapp.core.network

import com.benasher44.uuid.uuid4
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
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.logging.Logger as KtorLogger

/** Koin qualifier for the bare client used by the (disabled) backend auth endpoints. */
const val AUTH_HTTP_CLIENT = "authHttpClient"

/** Koin qualifier for the Zentrumhub location-autosuggest client (public, no credentials). */
const val ZENTRUMHUB_AUTOSUGGEST_CLIENT = "zentrumhubAutosuggestClient"

/** Koin qualifier for the authenticated Zentrumhub Nexus client (availability / booking). */
const val ZENTRUMHUB_NEXUS_CLIENT = "zentrumhubNexusClient"

/**
 * Headers redacted from request/response logs so tokens & provider credentials never reach the log.
 */
private val SENSITIVE_HEADERS = setOf(HttpHeaders.Authorization, "apiKey", "accountId")

class HttpClientFactory(
    private val config: NetworkConfig,
    private val zentrumhub: ZentrumhubConfig,
    private val logger: Logger,
) {

    // --- RestHalf backend: DISABLED. Kept for a future phone/password comeback; these builders are
    // not registered in DI (AppModule), so no client is ever built against the backend base URL. ---

    fun createAuthClient(): HttpClient = build(config.baseUrl, config.logRequests)

    fun createApiClient(tokenProvider: TokenProvider): HttpClient =
        build(config.baseUrl, config.logRequests) {
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

    // --- Zentrumhub: active. ---

    /** Zentrumhub autosuggest — public endpoint, no credentials (matches the provider spec). */
    fun createAutosuggestClient(): HttpClient = build(zentrumhub.autosuggestBaseUrl, zentrumhub.logRequests)

    /**
     * Zentrumhub Nexus — authenticated provider APIs (availability, booking, …). Sends the mandatory
     * accountId / apiKey on every request plus a fresh correlationId per call. Scaffolded here; the
     * actual endpoint calls land as each feature's Nexus spec is wired.
     */
    fun createNexusClient(): HttpClient = build(zentrumhub.nexusBaseUrl, zentrumhub.logRequests) {
        defaultRequest {
            header("accountId", zentrumhub.accountId)
            header("apiKey", zentrumhub.apiKey)
            // DefaultRequest re-runs its block per request, so each call gets a unique correlationId.
            header("correlationId", uuid4().toString())
            zentrumhub.customerIp?.let { header("customer-ip", it) }
            contentType(ContentType.Application.Json)
        }
    }

    private fun build(
        baseUrl: String,
        logRequests: Boolean,
        extra: HttpClientConfig<*>.() -> Unit = {},
    ): HttpClient = HttpClient {
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

        if (logRequests) {
            val appLogger = logger
            install(Logging) {
                level = LogLevel.ALL
                this.logger = object : KtorLogger {
                    override fun log(message: String) {
                        appLogger.info("Http", message)
                    }
                }
                // Keep bodies visible for debugging, but never print credentials.
                sanitizeHeader { name -> SENSITIVE_HEADERS.any { it.equals(name, ignoreCase = true) } }
            }
        }

        defaultRequest {
            url(baseUrl)
            header(HttpHeaders.Accept, ContentType.Application.Json.toString())
        }

        extra()
    }
}
