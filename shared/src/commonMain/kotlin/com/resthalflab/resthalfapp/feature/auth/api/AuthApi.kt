package com.resthalflab.resthalfapp.feature.auth.api

import com.resthalflab.resthalfapp.core.domain.AppResult
import kotlinx.coroutines.flow.StateFlow

interface AuthApi {
    val session: StateFlow<AuthSession?>

    // Local-first auth used by the MVP (no own backend).
    suspend fun signInWithGoogle(account: GoogleAccount)
    fun continueAsGuest()

    // Backend phone/password auth — kept for a future comeback; not wired in the MVP.
    suspend fun login(accountType: AccountType, phone: String, password: String): AppResult<Unit>
    suspend fun register(
        fullName: String,
        phone: String,
        email: String,
        password: String,
    ): AppResult<Unit>

    suspend fun logout()
}
