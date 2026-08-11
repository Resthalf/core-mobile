package com.resthalflab.resthalfapp.feature.auth.ui.login

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.auth.api.AccountType
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
        val accountType: AccountType = AccountType.Guest,
        val phone: String = "",
        val password: String = "",
        val phoneError: String? = null,
        val passwordError: String? = null,
        val submitting: Boolean = false,
        val generalError: String? = null,
    ) {
        val canSubmit: Boolean
            get() = phone.isNotBlank() && password.isNotBlank() && !submitting
    }

    sealed interface Event {
        data class AccountTypeChanged(val value: AccountType) : Event
        data class PhoneChanged(val value: String) : Event
        data class PasswordChanged(val value: String) : Event
        data object Submit : Event
        data object CreateAccount : Event
        data object DismissError : Event
    }
}

class DefaultLoginComponent(
    componentContext: ComponentContext,
    private val login: LoginUseCase,
    private val onNavigateToRegister: () -> Unit,
) : LoginComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(LoginComponent.UiState())
    override val state: StateFlow<LoginComponent.UiState> = _state.asStateFlow()

    override fun onEvent(event: LoginComponent.Event) {
        when (event) {
            is LoginComponent.Event.AccountTypeChanged ->
                _state.update { it.copy(accountType = event.value, generalError = null) }
            is LoginComponent.Event.PhoneChanged ->
                _state.update { it.copy(phone = event.value, phoneError = null, generalError = null) }
            is LoginComponent.Event.PasswordChanged ->
                _state.update { it.copy(password = event.value, passwordError = null, generalError = null) }
            LoginComponent.Event.DismissError ->
                _state.update { it.copy(generalError = null) }
            LoginComponent.Event.CreateAccount ->
                onNavigateToRegister()
            LoginComponent.Event.Submit ->
                submit()
        }
    }

    private fun submit() {
        val snapshot = _state.value
        scope.launch {
            _state.update { it.copy(submitting = true, generalError = null) }
            applyResult(login(snapshot.accountType, snapshot.phone, snapshot.password))
        }
    }

    private fun applyResult(result: AppResult<Unit>) {
        when (result) {
            is AppResult.Success -> _state.update { it.copy(submitting = false) }
            is AppResult.Failure -> when (val err = result.error) {
                is AppError.Validation -> _state.update {
                    it.copy(
                        submitting = false,
                        phoneError = if (err.field == "phone") err.message else it.phoneError,
                        passwordError = if (err.field == "password") err.message else it.passwordError,
                    )
                }
                else -> _state.update { it.copy(submitting = false, generalError = err.message) }
            }
        }
    }
}
