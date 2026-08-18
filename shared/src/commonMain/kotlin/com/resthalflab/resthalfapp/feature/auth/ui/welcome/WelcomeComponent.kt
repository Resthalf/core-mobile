package com.resthalflab.resthalfapp.feature.auth.ui.welcome

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.feature.auth.api.AuthApi
import com.resthalflab.resthalfapp.feature.auth.api.GoogleAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface WelcomeComponent {
    val state: StateFlow<UiState>
    fun onGoogleAccount(account: GoogleAccount)
    fun onGoogleCancelled()
    fun onContinueAsGuest()
    fun onDismissError()

    data class UiState(
        val submitting: Boolean = false,
        val error: String? = null,
    )
}

class DefaultWelcomeComponent(
    componentContext: ComponentContext,
    private val auth: AuthApi,
) : WelcomeComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(WelcomeComponent.UiState())
    override val state: StateFlow<WelcomeComponent.UiState> = _state.asStateFlow()

    override fun onGoogleAccount(account: GoogleAccount) {
        scope.launch {
            _state.update { it.copy(submitting = true, error = null) }
            // On success, auth.session flips and RootComponent navigates to Main (this component dies).
            runCatching { auth.signInWithGoogle(account) }
                .onFailure { err ->
                    _state.update { it.copy(submitting = false, error = err.message ?: "Sign-in failed") }
                }
        }
    }

    override fun onGoogleCancelled() = _state.update { it.copy(submitting = false) }

    override fun onContinueAsGuest() {
        _state.update { it.copy(submitting = true, error = null) }
        auth.continueAsGuest()
    }

    override fun onDismissError() = _state.update { it.copy(error = null) }
}
