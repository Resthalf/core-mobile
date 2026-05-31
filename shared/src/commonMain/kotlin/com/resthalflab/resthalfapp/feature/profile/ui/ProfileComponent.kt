package com.resthalflab.resthalfapp.feature.profile.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.feature.auth.api.AuthApi
import com.resthalflab.resthalfapp.feature.auth.api.AuthSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface ProfileComponent {
    val session: StateFlow<AuthSession?>
    fun onLogoutClicked()
}

class DefaultProfileComponent(
    componentContext: ComponentContext,
    private val auth: AuthApi,
) : ProfileComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    override val session: StateFlow<AuthSession?> = auth.session

    override fun onLogoutClicked() {
        scope.launch { auth.logout() }
    }
}
