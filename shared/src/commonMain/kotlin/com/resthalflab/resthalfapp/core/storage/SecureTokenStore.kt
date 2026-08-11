package com.resthalflab.resthalfapp.core.storage

data class AuthTokens(val accessToken: String, val refreshToken: String)

interface SecureTokenStore {
    fun read(): AuthTokens?
    fun save(tokens: AuthTokens)
    fun clear()
}
