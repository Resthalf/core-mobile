package com.resthalflab.resthalfapp.feature.auth.ui.welcome

import androidx.compose.runtime.Composable
import com.resthalflab.resthalfapp.feature.auth.api.GoogleAccount

// TODO(iOS): integrate the GoogleSignIn SDK + Firebase Auth. Stubbed until wired in Xcode.
@Composable
actual fun rememberGoogleSignIn(onResult: (GoogleAccount?) -> Unit): () -> Unit = {
    onResult(null)
}
