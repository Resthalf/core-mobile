package com.resthalflab.resthalfapp.feature.auth

import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
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
    var lastEmail: String? = null

    override suspend fun login(email: String, password: String): AppResult<Unit> {
        loginCalls++
        lastEmail = email
        return result
    }

    override suspend fun logout() { sessionState.value = null }
}

class LoginUseCaseTest {

    @Test
    fun blank_email_fails_validation_without_calling_api() = runTest {
        val api = FakeAuthApi()
        val useCase = LoginUseCase(api)

        val result = useCase("", "secret123")

        val failure = result.shouldBeInstanceOf<AppResult.Failure>()
        val error = failure.error.shouldBeInstanceOf<AppError.Validation>()
        error.field shouldBe "email"
        api.loginCalls shouldBe 0
    }

    @Test
    fun short_password_fails_validation() = runTest {
        val api = FakeAuthApi()
        val useCase = LoginUseCase(api)

        val result = useCase("user@resthalf.dev", "123")

        val failure = result.shouldBeInstanceOf<AppResult.Failure>()
        failure.error.shouldBeInstanceOf<AppError.Validation>().field shouldBe "password"
        api.loginCalls shouldBe 0
    }

    @Test
    fun valid_input_trims_email_and_delegates_to_api() = runTest {
        val api = FakeAuthApi(result = AppResult.Success(Unit))
        val useCase = LoginUseCase(api)

        val result = useCase("  user@resthalf.dev  ", "secret123")

        result.shouldBeInstanceOf<AppResult.Success<Unit>>()
        api.loginCalls shouldBe 1
        api.lastEmail shouldBe "user@resthalf.dev"
    }

    @Test
    fun api_failure_is_propagated() = runTest {
        val api = FakeAuthApi(result = AppResult.Failure(AppError.Auth.InvalidCredentials))
        val useCase = LoginUseCase(api)

        val result = useCase("user@resthalf.dev", "secret123")

        result.shouldBeInstanceOf<AppResult.Failure>()
            .error.shouldBeInstanceOf<AppError.Auth>()
    }
}
