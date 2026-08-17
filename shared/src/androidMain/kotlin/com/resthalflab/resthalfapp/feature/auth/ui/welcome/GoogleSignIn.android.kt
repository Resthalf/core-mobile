package com.resthalflab.resthalfapp.feature.auth.ui.welcome

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.resthalflab.resthalfapp.feature.auth.api.GoogleAccount
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Fallback if the google-services plugin's generated `default_web_client_id` resource is unavailable.
private const val DEFAULT_WEB_CLIENT_ID =
    "409088970818-1dpkig0duk6h6q3tieha332mnmogd5vj.apps.googleusercontent.com"

@Composable
actual fun rememberGoogleSignIn(onResult: (GoogleAccount?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return {
        scope.launch {
            val account = try {
                signInWithGoogle(context)
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                null // user cancelled the sheet, no credentials, or Firebase rejected the token
            }
            onResult(account)
        }
    }
}

private suspend fun signInWithGoogle(context: Context): GoogleAccount? {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(context.webClientId())
        .setAutoSelectEnabled(false)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    val response = CredentialManager.create(context).getCredential(context, request)
    val credential = response.credential
    if (credential !is CustomCredential ||
        credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        return null
    }

    val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
    val authResult = FirebaseAuth.getInstance()
        .signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
        .await()
    val user = authResult.user ?: return null
    return GoogleAccount(
        uid = user.uid,
        displayName = user.displayName,
        email = user.email,
        photoUrl = user.photoUrl?.toString(),
    )
}

private fun Context.webClientId(): String {
    val resId = resources.getIdentifier("default_web_client_id", "string", packageName)
    return if (resId != 0) getString(resId) else DEFAULT_WEB_CLIENT_ID
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
    addOnCanceledListener { cont.cancel() }
}
