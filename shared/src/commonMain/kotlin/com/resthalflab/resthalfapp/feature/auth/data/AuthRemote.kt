package com.resthalflab.resthalfapp.feature.auth.data

import com.resthalflab.resthalfapp.feature.auth.data.dto.TokenResponse
import io.ktor.client.HttpClient
import kotlinx.coroutines.delay

// Phase 1 stub. Replace these stubs with real Ktor calls when the backend lands:
//   client.post("auth/login") { setBody(LoginRequest(email, password)) }.body<TokenResponse>()
class AuthRemote(
    @Suppress("unused") private val client: HttpClient,
) {
    suspend fun login(email: String, password: String): TokenResponse {
        delay(500)
        return TokenResponse(
            accessToken = "stub.access.token.${email.hashCode()}",
            refreshToken = "stub.refresh.token.${email.hashCode()}",
            userId = "stub-user-${email.hashCode()}",
            email = email,
        )
    }

    suspend fun refresh(currentRefresh: String): TokenResponse {
        delay(200)
        return TokenResponse(
            accessToken = "stub.access.rotated.${currentRefresh.hashCode()}",
            refreshToken = "stub.refresh.rotated.${currentRefresh.hashCode()}",
            userId = "stub-user",
            email = "stub@resthalf.dev",
        )
    }
}
