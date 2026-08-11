package com.resthalflab.resthalfapp.feature.auth.ui.register

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.auth.domain.RegisterUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface RegisterComponent {
    val state: StateFlow<UiState>
    fun onEvent(event: Event)

    data class UiState(
        val fullName: String = "",
        val phone: String = "",
        val email: String = "",
        val password: String = "",
        val fullNameError: String? = null,
        val phoneError: String? = null,
        val emailError: String? = null,
        val passwordError: String? = null,
        val submitting: Boolean = false,
        val generalError: String? = null,
    ) {
        val canSubmit: Boolean
            get() = fullName.isNotBlank() && phone.isNotBlank() &&
                email.isNotBlank() && password.isNotBlank() && !submitting
    }

    sealed interface Event {
        data class FullNameChanged(val value: String) : Event
        data class PhoneChanged(val value: String) : Event
        data class EmailChanged(val value: String) : Event
        data class PasswordChanged(val value: String) : Event
        data object Submit : Event
        data object BackToLogin : Event
    }
}

class DefaultRegisterComponent(
    componentContext: ComponentContext,
    private val register: RegisterUseCase,
    private val onBack: () -> Unit,
) : RegisterComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(RegisterComponent.UiState())
    override val state: StateFlow<RegisterComponent.UiState> = _state.asStateFlow()

    override fun onEvent(event: RegisterComponent.Event) {
        when (event) {
            is RegisterComponent.Event.FullNameChanged ->
                _state.update { it.copy(fullName = event.value, fullNameError = null, generalError = null) }
            is RegisterComponent.Event.PhoneChanged ->
                _state.update { it.copy(phone = event.value, phoneError = null, generalError = null) }
            is RegisterComponent.Event.EmailChanged ->
                _state.update { it.copy(email = event.value, emailError = null, generalError = null) }
            is RegisterComponent.Event.PasswordChanged ->
                _state.update { it.copy(password = event.value, passwordError = null, generalError = null) }
            RegisterComponent.Event.BackToLogin ->
                onBack()
            RegisterComponent.Event.Submit ->
                submit()
        }
    }

    // On success the AuthApi session flips to non-null, so the root navigates to Main automatically.
    private fun submit() {
        val s = _state.value
        scope.launch {
            _state.update { it.copy(submitting = true, generalError = null) }
            applyResult(register(s.fullName, s.phone, s.email, s.password))
        }
    }

    private fun applyResult(result: AppResult<Unit>) {
        when (result) {
            is AppResult.Success -> _state.update { it.copy(submitting = false) }
            is AppResult.Failure -> when (val err = result.error) {
                is AppError.Validation -> _state.update {
                    it.copy(
                        submitting = false,
                        fullNameError = if (err.field == "fullName") err.message else it.fullNameError,
                        phoneError = if (err.field == "phone") err.message else it.phoneError,
                        emailError = if (err.field == "email") err.message else it.emailError,
                        passwordError = if (err.field == "password") err.message else it.passwordError,
                    )
                }
                else -> _state.update { it.copy(submitting = false, generalError = err.message) }
            }
        }
    }
}
