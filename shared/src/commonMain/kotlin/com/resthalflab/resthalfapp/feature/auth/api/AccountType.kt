package com.resthalflab.resthalfapp.feature.auth.api

import kotlinx.serialization.Serializable

/** Which login flow / API route to use. [route] feeds "/auth/{route}/login". */
@Serializable
enum class AccountType(val route: String) {
    Guest("guest"),
    Staff("staff"),
}
