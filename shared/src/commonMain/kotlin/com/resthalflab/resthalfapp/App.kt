package com.resthalflab.resthalfapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.resthalflab.resthalfapp.app.HomeComponent
import com.resthalflab.resthalfapp.app.RootComponent
import com.resthalflab.resthalfapp.core.design.ResthalfTheme
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhScaffold
import com.resthalflab.resthalfapp.feature.auth.ui.login.LoginScreen

@Composable
fun App(rootComponent: RootComponent) {
    ResthalfTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Children(stack = rootComponent.childStack) { child ->
                when (val instance = child.instance) {
                    is RootComponent.Child.Login -> LoginScreen(instance.component)
                    is RootComponent.Child.Home -> HomeScreen(instance.component)
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(component: HomeComponent) {
    val session by component.session.collectAsStateWithLifecycle()
    RhScaffold(title = "Resthalf") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Welcome${session?.email?.let { ", $it" } ?: ""}",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Running on ${component.platformName}",
                style = MaterialTheme.typography.bodyMedium,
            )
            RhButton(text = "Sign out", onClick = component::onLogoutClicked)
        }
    }
}
