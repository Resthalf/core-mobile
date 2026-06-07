package com.resthalflab.resthalfapp.feature.auth.api

import kotlinx.serialization.Serializable

@Serializable
data class AuthSession(
    val id: String,
    val displayName: String,
    val phone: String,
    val accountType: AccountType,
    val email: String? = null,
    // Staff-only
    val role: String? = null,
    val hotelId: String? = null,
)
