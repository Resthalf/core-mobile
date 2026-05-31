package com.resthalflab.resthalfapp.feature.auth.data

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.network.safeApiCall
import com.resthalflab.resthalfapp.core.storage.AuthTokens
import com.resthalflab.resthalfapp.core.storage.SecureTokenStore
import com.resthalflab.resthalfapp.feature.auth.api.AuthApi
import com.resthalflab.resthalfapp.feature.auth.api.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(
    private val remote: AuthRemote,
    private val tokenStore: SecureTokenStore,
) : AuthApi {

    private val _session = MutableStateFlow<AuthSession?>(null)
    override val session: StateFlow<AuthSession?> = _session.asStateFlow()

    init {
        // Restore session if tokens are persisted. Real userId/email would come from a /me call or JWT decode;
        // stubbed for Phase 1.
        tokenStore.read()?.let {
            _session.value = AuthSession(userId = "restored", email = "restored@resthalf.dev")
        }
    }

    override suspend fun login(email: String, password: String): AppResult<Unit> = safeApiCall {
        val response = remote.login(email, password)
        tokenStore.save(AuthTokens(response.accessToken, response.refreshToken))
        _session.value = AuthSession(userId = response.userId, email = response.email)
    }

    override suspend fun logout() {
        tokenStore.clear()
        _session.value = null
    }
}
