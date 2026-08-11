package com.resthalflab.resthalfapp.feature.auth.ui.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhPasswordField
import com.resthalflab.resthalfapp.core.design.components.RhTextButton
import com.resthalflab.resthalfapp.core.design.components.RhTextField
import com.resthalflab.resthalfapp.core.design.components.RhWaveHeader
import com.resthalflab.resthalfapp.core.design.components.RhWordmark

@Composable
fun RegisterScreen(component: RegisterComponent) {
    val state by component.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        RhWaveHeader(height = 240.dp) {
            RhWordmark(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp),
                restColor = MaterialTheme.colorScheme.onPrimaryContainer,
                accentColor = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.displaySmall,
            )
        }

        Column(modifier = Modifier.padding(horizontal = RhSpacing.xl)) {
            Text(
                text = "Create account",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Sign up to book your day room",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(RhSpacing.lg))
            RhTextField(
                value = state.fullName,
                onValueChange = { component.onEvent(RegisterComponent.Event.FullNameChanged(it)) },
                placeholder = "Full name",
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.submitting,
                isError = state.fullNameError != null,
                errorText = state.fullNameError,
                leadingIcon = Icons.Outlined.Person,
                imeAction = ImeAction.Next,
            )

            Spacer(Modifier.height(RhSpacing.md))
            RhTextField(
                value = state.phone,
                onValueChange = { component.onEvent(RegisterComponent.Event.PhoneChanged(it)) },
                placeholder = "Phone number",
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.submitting,
                isError = state.phoneError != null,
                errorText = state.phoneError,
                leadingIcon = Icons.Outlined.Phone,
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next,
            )

            Spacer(Modifier.height(RhSpacing.md))
            RhTextField(
                value = state.email,
                onValueChange = { component.onEvent(RegisterComponent.Event.EmailChanged(it)) },
                placeholder = "Email",
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.submitting,
                isError = state.emailError != null,
                errorText = state.emailError,
                leadingIcon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            )

            Spacer(Modifier.height(RhSpacing.md))
            RhPasswordField(
                value = state.password,
                onValueChange = { component.onEvent(RegisterComponent.Event.PasswordChanged(it)) },
                placeholder = "Password",
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.submitting,
                isError = state.passwordError != null,
                errorText = state.passwordError,
                leadingIcon = Icons.Outlined.Lock,
                imeAction = ImeAction.Done,
                onImeAction = { component.onEvent(RegisterComponent.Event.Submit) },
            )

            state.generalError?.let { msg ->
                Spacer(Modifier.height(RhSpacing.sm))
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(RhSpacing.xxl))
            RhButton(
                text = "Register",
                onClick = { component.onEvent(RegisterComponent.Event.Submit) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSubmit,
                loading = state.submitting,
                shape = RoundedCornerShape(percent = 50),
            )

            Spacer(Modifier.height(RhSpacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                RhTextButton(
                    text = "Sign in",
                    onClick = { component.onEvent(RegisterComponent.Event.BackToLogin) },
                )
            }
            Spacer(Modifier.height(RhSpacing.lg))
        }
    }
}
