package com.resthalflab.resthalfapp.feature.auth.ui.login

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.auth.domain.LoginUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface LoginComponent {
    val state: StateFlow<UiState>
    fun onEvent(event: Event)

    data class UiState(
        val email: String = "",
        val password: String = "",
        val emailError: String? = null,
        val passwordError: String? = null,
        val submitting: Boolean = false,
        val generalError: String? = null,
    ) {
        val canSubmit: Boolean
            get() = email.isNotBlank() && password.isNotBlank() && !submitting
    }

    sealed interface Event {
        data class EmailChanged(val value: String) : Event
        data class PasswordChanged(val value: String) : Event
        data object Submit : Event
        data object DismissError : Event
    }
}

class DefaultLoginComponent(
    componentContext: ComponentContext,
    private val login: LoginUseCase,
) : LoginComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(LoginComponent.UiState())
    override val state: StateFlow<LoginComponent.UiState> = _state.asStateFlow()

    override fun onEvent(event: LoginComponent.Event) {
        when (event) {
            is LoginComponent.Event.EmailChanged ->
                _state.update { it.copy(email = event.value, emailError = null, generalError = null) }
            is LoginComponent.Event.PasswordChanged ->
                _state.update { it.copy(password = event.value, passwordError = null, generalError = null) }
            LoginComponent.Event.DismissError ->
                _state.update { it.copy(generalError = null) }
            LoginComponent.Event.Submit ->
                submit()
        }
    }

    private fun submit() {
        val snapshot = _state.value
        scope.launch {
            _state.update { it.copy(submitting = true, generalError = null) }
            val result = login(snapshot.email, snapshot.password)
            applyResult(result)
        }
    }

    private fun applyResult(result: AppResult<Unit>) {
        when (result) {
            is AppResult.Success -> _state.update { it.copy(submitting = false) }
            is AppResult.Failure -> when (val err = result.error) {
                is AppError.Validation -> _state.update {
                    it.copy(
                        submitting = false,
                        emailError = if (err.field == "email") err.message else it.emailError,
                        passwordError = if (err.field == "password") err.message else it.passwordError,
                    )
                }
                else -> _state.update { it.copy(submitting = false, generalError = err.message) }
            }
        }
    }
}
