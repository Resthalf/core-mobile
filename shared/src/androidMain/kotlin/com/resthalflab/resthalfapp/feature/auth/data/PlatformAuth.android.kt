package com.resthalflab.resthalfapp.feature.auth.data

import com.google.firebase.auth.FirebaseAuth

actual fun signOutPlatformAuth() {
    runCatching { FirebaseAuth.getInstance().signOut() }
}
