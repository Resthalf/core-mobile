package com.resthalflab.resthalfapp.feature.auth.domain

import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.auth.api.AuthApi

class LoginUseCase(
    private val auth: AuthApi,
) {
    suspend operator fun invoke(email: String, password: String): AppResult<Unit> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || !trimmedEmail.contains('@')) {
            return AppResult.Failure(AppError.Validation("email", "Enter a valid email"))
        }
        if (password.length < 6) {
            return AppResult.Failure(AppError.Validation("password", "Password must be at least 6 characters"))
        }
        return auth.login(trimmedEmail, password)
    }
}
