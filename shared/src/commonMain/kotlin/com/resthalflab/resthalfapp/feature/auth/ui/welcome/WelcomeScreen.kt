package com.resthalflab.resthalfapp.feature.auth.ui.welcome

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhOutlinedButton
import com.resthalflab.resthalfapp.core.design.components.RhWaveHeader
import com.resthalflab.resthalfapp.core.design.components.RhWordmark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(component: WelcomeComponent) {
    val state by component.state.collectAsStateWithLifecycle()
    val googleSignIn = rememberGoogleSignIn { account ->
        if (account != null) component.onGoogleAccount(account) else component.onGoogleCancelled()
    }

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
                text = "Welcome to RestHalf",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(RhSpacing.xs))
            Text(
                text = "Day rooms for rest, work or layover — sign in to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(RhSpacing.xxl))
            RhButton(
                text = "Continue with Google",
                onClick = googleSignIn,
                modifier = Modifier.fillMaxWidth(),
                loading = state.submitting,
                shape = RoundedCornerShape(percent = 50),
            )

            Spacer(Modifier.height(RhSpacing.md))
            RhOutlinedButton(
                text = "Continue as guest",
                onClick = component::onContinueAsGuest,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.submitting,
            )

            state.error?.let { message ->
                Spacer(Modifier.height(RhSpacing.md))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(RhSpacing.xxl))
            Text(
                text = "By continuing you agree to our Terms & Privacy Policy.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(RhSpacing.lg))
        }
    }
}
