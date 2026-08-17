package com.resthalflab.resthalfapp.feature.auth.ui.welcome

import androidx.compose.runtime.Composable
import com.resthalflab.resthalfapp.feature.auth.api.GoogleAccount

/**
 * Platform Google sign-in. Returns a click handler that launches the native flow and delivers the
 * resulting [GoogleAccount] (or null on cancel/failure) to [onResult]. Android = Credential Manager
 * + Firebase Auth; iOS = stub until the GoogleSignIn SDK is wired.
 */
@Composable
expect fun rememberGoogleSignIn(onResult: (GoogleAccount?) -> Unit): () -> Unit
