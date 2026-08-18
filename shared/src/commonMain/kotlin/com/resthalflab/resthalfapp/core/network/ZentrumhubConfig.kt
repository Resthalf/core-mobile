package com.resthalflab.resthalfapp.core.network

/**
 * Connection settings for Zentrumhub, the external (wholesale) booking engine.
 *
 * Autosuggest ([autosuggestBaseUrl]) is a public endpoint and needs no credentials. The Nexus APIs
 * ([nexusBaseUrl] — availability, booking, …) require [accountId] + [apiKey], which currently ship
 * direct-from-mobile per product decision.
 *
 * ⚠️ Inject [accountId] / [apiKey] from build config — never commit real credentials to source.
 * Revisit proxying these behind the Resthalf backend before launch: a key in the app binary is
 * extractable. [logRequests] must stay false in release; [HttpClientFactory] also redacts the
 * apiKey/accountId headers from logs regardless.
 */
data class ZentrumhubConfig(
    val autosuggestBaseUrl: String,
    val nexusBaseUrl: String,
    val accountId: String,
    val apiKey: String,
    val channelId: String,
    val customerIp: String? = null,
    val logRequests: Boolean = false,
)
