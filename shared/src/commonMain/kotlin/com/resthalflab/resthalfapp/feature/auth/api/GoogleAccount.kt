package com.resthalflab.resthalfapp.feature.auth.api

/** Identity returned by platform Google/Firebase sign-in, mapped into a local [AuthSession]. */
data class GoogleAccount(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
)
