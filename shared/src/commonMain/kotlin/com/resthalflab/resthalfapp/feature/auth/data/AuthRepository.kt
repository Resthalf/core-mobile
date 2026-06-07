package com.resthalflab.resthalfapp.feature.auth.data

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.network.safeApiCall
import com.resthalflab.resthalfapp.core.storage.AuthTokens
import com.resthalflab.resthalfapp.core.storage.SecureTokenStore
import com.resthalflab.resthalfapp.core.storage.SettingsFactory
import com.resthalflab.resthalfapp.feature.auth.api.AccountType
import com.resthalflab.resthalfapp.feature.auth.api.AuthApi
import com.resthalflab.resthalfapp.feature.auth.api.AuthSession
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthRepository(
    private val remote: AuthRemote,
    private val tokenStore: SecureTokenStore,
    settingsFactory: SettingsFactory,
) : AuthApi {

    private val settings: Settings = settingsFactory.create("resthalf.auth")
    private val json = Json { ignoreUnknownKeys = true }

    private val _session = MutableStateFlow<AuthSession?>(null)
    override val session: StateFlow<AuthSession?> = _session.asStateFlow()

    init {
        // Restore session only if both a token and a cached profile are present.
        if (tokenStore.read() != null) {
            settings.getStringOrNull(KEY_SESSION)
                ?.let { runCatching { json.decodeFromString<AuthSession>(it) }.getOrNull() }
                ?.let { _session.value = it }
        }
    }

    override suspend fun login(accountType: AccountType, phone: String, password: String): AppResult<Unit> =
        safeApiCall { persist(remote.login(accountType, phone, password)) }

    override suspend fun register(
        fullName: String,
        phone: String,
        email: String,
        password: String,
    ): AppResult<Unit> = safeApiCall {
        persist(remote.register(fullName, phone, email, password))
    }

    override suspend fun logout() {
        tokenStore.clear()
        settings.remove(KEY_SESSION)
        _session.value = null
    }

    private fun persist(result: AuthResult) {
        // Single JWT — store it as both access & refresh slots so the Bearer plugin can attach it.
        tokenStore.save(AuthTokens(accessToken = result.token, refreshToken = result.token))
        settings.putString(KEY_SESSION, json.encodeToString(result.session))
        _session.value = result.session
    }

    private companion object {
        const val KEY_SESSION = "auth.session"
    }
}
