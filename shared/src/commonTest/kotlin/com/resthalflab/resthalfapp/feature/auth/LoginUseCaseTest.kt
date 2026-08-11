package com.resthalflab.resthalfapp.feature.auth

import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.auth.api.AccountType
import com.resthalflab.resthalfapp.feature.auth.api.AuthApi
import com.resthalflab.resthalfapp.feature.auth.api.AuthSession
import com.resthalflab.resthalfapp.feature.auth.domain.LoginUseCase
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

private class FakeAuthApi(
    var result: AppResult<Unit> = AppResult.Success(Unit),
) : AuthApi {
    val sessionState = MutableStateFlow<AuthSession?>(null)
    override val session: StateFlow<AuthSession?> = sessionState
    var loginCalls = 0
    var lastPhone: String? = null
    var lastAccountType: AccountType? = null

    override suspend fun login(accountType: AccountType, phone: String, password: String): AppResult<Unit> {
        loginCalls++
        lastPhone = phone
        lastAccountType = accountType
        return result
    }

    override suspend fun register(
        fullName: String,
        phone: String,
        email: String,
        password: String,
    ): AppResult<Unit> = result

    override suspend fun logout() { sessionState.value = null }
}

class LoginUseCaseTest {

    @Test
    fun blank_phone_fails_validation_without_calling_api() = runTest {
        val api = FakeAuthApi()
        val useCase = LoginUseCase(api)

        val result = useCase(AccountType.Guest, "", "secret123")

        val failure = result.shouldBeInstanceOf<AppResult.Failure>()
        failure.error.shouldBeInstanceOf<AppError.Validation>().field shouldBe "phone"
        api.loginCalls shouldBe 0
    }

    @Test
    fun blank_password_fails_validation() = runTest {
        val api = FakeAuthApi()
        val useCase = LoginUseCase(api)

        val result = useCase(AccountType.Guest, "+6289876543210", "")

        val failure = result.shouldBeInstanceOf<AppResult.Failure>()
        failure.error.shouldBeInstanceOf<AppError.Validation>().field shouldBe "password"
        api.loginCalls shouldBe 0
    }

    @Test
    fun valid_input_trims_phone_and_delegates_to_api() = runTest {
        val api = FakeAuthApi(result = AppResult.Success(Unit))
        val useCase = LoginUseCase(api)

        val result = useCase(AccountType.Staff, "  +6289876543210  ", "guest123")

        result.shouldBeInstanceOf<AppResult.Success<Unit>>()
        api.loginCalls shouldBe 1
        api.lastPhone shouldBe "+6289876543210"
        api.lastAccountType shouldBe AccountType.Staff
    }

    @Test
    fun api_failure_is_propagated() = runTest {
        val api = FakeAuthApi(result = AppResult.Failure(AppError.Auth.InvalidCredentials))
        val useCase = LoginUseCase(api)

        val result = useCase(AccountType.Guest, "+6289876543210", "guest123")

        result.shouldBeInstanceOf<AppResult.Failure>()
            .error.shouldBeInstanceOf<AppError.Auth>()
    }
}
