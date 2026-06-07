package com.resthalflab.resthalfapp.feature.auth.domain

import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.auth.api.AccountType
import com.resthalflab.resthalfapp.feature.auth.api.AuthApi

class LoginUseCase(
    private val auth: AuthApi,
) {
    suspend operator fun invoke(
        accountType: AccountType,
        phone: String,
        password: String,
    ): AppResult<Unit> {
        val trimmedPhone = phone.trim()
        if (trimmedPhone.isEmpty()) {
            return AppResult.Failure(AppError.Validation("phone", "Enter your phone number"))
        }
        if (password.isEmpty()) {
            return AppResult.Failure(AppError.Validation("password", "Enter your password"))
        }
        return auth.login(accountType, trimmedPhone, password)
    }
}
