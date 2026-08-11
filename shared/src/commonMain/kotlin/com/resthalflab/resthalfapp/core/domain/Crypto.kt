package com.resthalflab.resthalfapp.core.domain

/** Lowercase hex SHA-512 of [input]. Platform-backed (no extra dependency). */
expect fun sha512Hex(input: String): String
