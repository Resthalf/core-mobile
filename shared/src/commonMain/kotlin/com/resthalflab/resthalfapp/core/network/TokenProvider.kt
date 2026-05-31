package com.resthalflab.resthalfapp.core.network

data class BearerTokenPair(val access: String, val refresh: String)

interface TokenProvider {
    suspend fun load(): BearerTokenPair?
    suspend fun refresh(currentRefresh: String): BearerTokenPair?
}
