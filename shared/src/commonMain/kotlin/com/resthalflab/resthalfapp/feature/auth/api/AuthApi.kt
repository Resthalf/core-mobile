package com.resthalflab.resthalfapp.feature.auth.api

import com.resthalflab.resthalfapp.core.domain.AppResult
import kotlinx.coroutines.flow.StateFlow

interface AuthApi {
    val session: StateFlow<AuthSession?>

    suspend fun login(email: String, password: String): AppResult<Unit>
    suspend fun logout()
}
