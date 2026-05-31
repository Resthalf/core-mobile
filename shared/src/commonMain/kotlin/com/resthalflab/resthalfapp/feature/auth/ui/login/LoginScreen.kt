package com.resthalflab.resthalfapp.feature.auth.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhPasswordField
import com.resthalflab.resthalfapp.core.design.components.RhScaffold
import com.resthalflab.resthalfapp.core.design.components.RhTextField
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun LoginScreen(component: LoginComponent) {
    val state by component.state.collectAsStateWithLifecycle()

    RhScaffold(title = "Sign in") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Welcome to Resthalf", style = MaterialTheme.typography.headlineSmall)

            RhTextField(
                value = state.email,
                onValueChange = { component.onEvent(LoginComponent.Event.EmailChanged(it)) },
                label = "Email",
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.submitting,
                isError = state.emailError != null,
                errorText = state.emailError,
                keyboardType = KeyboardType.Email,
            )

            RhPasswordField(
                value = state.password,
                onValueChange = { component.onEvent(LoginComponent.Event.PasswordChanged(it)) },
                label = "Password",
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.submitting,
                isError = state.passwordError != null,
                errorText = state.passwordError,
            )

            state.generalError?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            RhButton(
                text = "Sign in",
                onClick = { component.onEvent(LoginComponent.Event.Submit) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSubmit,
                loading = state.submitting,
            )
        }
    }
}
