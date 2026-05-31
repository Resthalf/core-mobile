package com.resthalflab.resthalfapp.app

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.resthalflab.resthalfapp.feature.auth.api.AuthApi
import com.resthalflab.resthalfapp.feature.auth.api.AuthSession
import com.resthalflab.resthalfapp.getPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface HomeComponent {
    val platformName: String
    val session: StateFlow<AuthSession?>
    fun onLogoutClicked()
}

class DefaultHomeComponent(
    componentContext: ComponentContext,
    private val auth: AuthApi,
) : HomeComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    override val platformName: String = getPlatform().name
    override val session = auth.session

    override fun onLogoutClicked() {
        scope.launch { auth.logout() }
    }
}
