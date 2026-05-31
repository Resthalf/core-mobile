package com.resthalflab.resthalfapp.feature.auth.data

import com.resthalflab.resthalfapp.core.network.BearerTokenPair
import com.resthalflab.resthalfapp.core.network.TokenProvider
import com.resthalflab.resthalfapp.core.storage.AuthTokens
import com.resthalflab.resthalfapp.core.storage.SecureTokenStore

class AuthTokenProvider(
    private val tokenStore: SecureTokenStore,
    private val remote: AuthRemote,
) : TokenProvider {

    override suspend fun load(): BearerTokenPair? =
        tokenStore.read()?.let { BearerTokenPair(it.accessToken, it.refreshToken) }

    override suspend fun refresh(currentRefresh: String): BearerTokenPair? = try {
        val response = remote.refresh(currentRefresh)
        tokenStore.save(AuthTokens(response.accessToken, response.refreshToken))
        BearerTokenPair(response.accessToken, response.refreshToken)
    } catch (t: Throwable) {
        tokenStore.clear()
        null
    }
}
