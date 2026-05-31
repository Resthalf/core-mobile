package com.resthalflab.resthalfapp.core.network

data class NetworkConfig(
    val baseUrl: String,
    val connectTimeoutMs: Long = 15_000,
    val requestTimeoutMs: Long = 30_000,
    val logRequests: Boolean = true,
)
