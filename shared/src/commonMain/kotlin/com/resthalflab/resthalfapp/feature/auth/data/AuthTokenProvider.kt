package com.resthalflab.resthalfapp.feature.auth.data

import com.resthalflab.resthalfapp.core.network.BearerTokenPair
import com.resthalflab.resthalfapp.core.network.TokenProvider
import com.resthalflab.resthalfapp.core.storage.SecureTokenStore

class AuthTokenProvider(
    private val tokenStore: SecureTokenStore,
) : TokenProvider {

    override suspend fun load(): BearerTokenPair? =
        tokenStore.read()?.let { BearerTokenPair(it.accessToken, it.refreshToken) }

    // The backend issues a single JWT with no refresh endpoint. On a 401 we can't refresh, so clear
    // the token and let the request fail — the user re-authenticates. (Revisit if a refresh API lands.)
    override suspend fun refresh(currentRefresh: String): BearerTokenPair? {
        tokenStore.clear()
        return null
    }
}
