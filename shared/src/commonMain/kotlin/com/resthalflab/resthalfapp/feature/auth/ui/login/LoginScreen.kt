package com.resthalflab.resthalfapp.feature.auth.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.resthalflab.resthalfapp.feature.auth.api.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(component: LoginComponent) {
    val state by component.state.collectAsStateWithLifecycle()
    var rememberMe by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        RhWaveHeader(height = 300.dp) {
            RhWordmark(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp, start = RhSpacing.xl),
                restColor = MaterialTheme.colorScheme.onPrimaryContainer,
                accentColor = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.displaySmallEmphasized,
            )
        }

        Column(modifier = Modifier.padding(horizontal = RhSpacing.xl)) {
            Text(
                text = "Welcome Back!",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Sign in to your account",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(RhSpacing.lg))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                AccountType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = state.accountType == type,
                        onClick = { component.onEvent(LoginComponent.Event.AccountTypeChanged(type)) },
                        enabled = !state.submitting,
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = AccountType.entries.size),
                    ) {
                        Text(type.name)
                    }
                }
            }

            Spacer(Modifier.height(RhSpacing.lg))
            RhTextField(
                value = state.phone,
                onValueChange = { component.onEvent(LoginComponent.Event.PhoneChanged(it)) },
                placeholder = "Enter your phone number",
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.submitting,
                isError = state.phoneError != null,
                errorText = state.phoneError,
                leadingIcon = Icons.Outlined.Phone,
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next,
            )

            Spacer(Modifier.height(RhSpacing.md))
            RhPasswordField(
                value = state.password,
                onValueChange = { component.onEvent(LoginComponent.Event.PasswordChanged(it)) },
                placeholder = "Enter your password",
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.submitting,
                isError = state.passwordError != null,
                errorText = state.passwordError,
                leadingIcon = Icons.Outlined.Lock,
                imeAction = ImeAction.Done,
                onImeAction = { component.onEvent(LoginComponent.Event.Submit) },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                    Text("Remember me", style = MaterialTheme.typography.bodyMedium)
                }
                RhTextButton(text = "Forgot password?", onClick = { /* TODO Phase 3 */ })
            }

            state.generalError?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(RhSpacing.xxl))
            RhButton(
                text = "Login",
                onClick = { component.onEvent(LoginComponent.Event.Submit) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSubmit,
                loading = state.submitting,
                shape = RoundedCornerShape(percent = 50),
            )

            Spacer(Modifier.height(RhSpacing.xxl))
            DividerWithText("or continue with")
            Spacer(Modifier.height(RhSpacing.xl))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(RhSpacing.lg, Alignment.CenterHorizontally),
            ) {
                // TODO: replace letter placeholders with real brand assets.
                SocialButton("G")
                SocialButton("f")
                SocialButton("t")
            }

            Spacer(Modifier.height(RhSpacing.xxl))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                RhTextButton(
                    text = "Sign up",
                    onClick = { component.onEvent(LoginComponent.Event.CreateAccount) },
                )
            }
            Spacer(Modifier.height(RhSpacing.lg))
        }
    }
}

@Composable
private fun DividerWithText(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RhSpacing.md),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SocialButton(label: String) {
    Surface(
        onClick = { /* TODO Phase 3: social sign-in */ },
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
