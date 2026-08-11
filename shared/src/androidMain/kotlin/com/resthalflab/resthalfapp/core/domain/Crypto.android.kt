package com.resthalflab.resthalfapp.core.domain

import java.security.MessageDigest

actual fun sha512Hex(input: String): String =
    MessageDigest.getInstance("SHA-512")
        .digest(input.encodeToByteArray())
        .joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
