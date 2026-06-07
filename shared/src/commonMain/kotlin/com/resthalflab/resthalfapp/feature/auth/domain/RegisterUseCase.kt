package com.resthalflab.resthalfapp.feature.auth.domain

import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.auth.api.AuthApi

class RegisterUseCase(
    private val auth: AuthApi,
) {
    suspend operator fun invoke(
        fullName: String,
        phone: String,
        email: String,
        password: String,
    ): AppResult<Unit> {
        val name = fullName.trim()
        val trimmedPhone = phone.trim()
        val trimmedEmail = email.trim()

        if (name.isEmpty()) {
            return AppResult.Failure(AppError.Validation("fullName", "Enter your full name"))
        }
        if (trimmedPhone.isEmpty()) {
            return AppResult.Failure(AppError.Validation("phone", "Enter your phone number"))
        }
        if (trimmedEmail.isEmpty() || !trimmedEmail.contains('@')) {
            return AppResult.Failure(AppError.Validation("email", "Enter a valid email"))
        }
        if (password.length < 8) {
            return AppResult.Failure(AppError.Validation("password", "Password must be at least 8 characters"))
        }
        return auth.register(name, trimmedPhone, trimmedEmail, password)
    }
}
