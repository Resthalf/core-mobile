package com.resthalflab.resthalfapp.feature.auth.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.components.RhButton
import com.resthalflab.resthalfapp.core.design.components.RhOutlinedButton
import org.jetbrains.compose.resources.painterResource
import resthalfapp.shared.generated.resources.Res
import resthalfapp.shared.generated.resources.resthalf_logo
import resthalfapp.shared.generated.resources.welcome_background

@Composable
fun WelcomeScreen(component: WelcomeComponent) {
    val state by component.state.collectAsStateWithLifecycle()
    val googleSignIn = rememberGoogleSignIn { account ->
        if (account != null) component.onGoogleAccount(account) else component.onGoogleCancelled()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        WelcomeHero()

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = RhSpacing.xl)) {
            Text(
                text = "Ready when you are.",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(RhSpacing.sm))
            Text(
                text = buildAnnotatedString {
                    append("Book day rooms for rest, work, or a layover — ")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)) {
                        append("let's get you in.")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(RhSpacing.xxl))
            RhButton(
                text = "Continue with Google",
                onClick = googleSignIn,
                modifier = Modifier.fillMaxWidth(),
                loading = state.submitting,
            )

            Spacer(Modifier.height(RhSpacing.lg))
            OrDivider()
            Spacer(Modifier.height(RhSpacing.lg))

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
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(RhSpacing.lg))
        }
    }
}

/**
 * Brand hero: the RestHalf logo over a room photo that fades down into the page background so the
 * banner blends seamlessly into the content below.
 */
@Composable
private fun WelcomeHero() {
    Box(modifier = Modifier.fillMaxWidth().height(380.dp)) {
        Image(
            painter = painterResource(Res.drawable.welcome_background),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
        // Soft wash + fade to the page background so the logo reads and the image melts into content.
        Box(
            modifier = Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    0.0f to MaterialTheme.colorScheme.background.copy(alpha = 0.20f),
                    0.55f to MaterialTheme.colorScheme.background.copy(alpha = 0.60f),
                    1.0f to MaterialTheme.colorScheme.background,
                )
            ),
        )
        Image(
            painter = painterResource(Res.drawable.resthalf_logo),
            contentDescription = "RestHalf",
            modifier = Modifier
                .align(Alignment.Center)
                .statusBarsPadding()
                .padding(top = 100.dp)
                .fillMaxWidth(0.6f),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun OrDivider() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text = "or",
            modifier = Modifier.padding(horizontal = RhSpacing.md),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}
