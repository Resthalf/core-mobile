package com.resthalflab.resthalfapp.core.domain

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA512
import platform.CoreCrypto.CC_SHA512_DIGEST_LENGTH

@OptIn(ExperimentalForeignApi::class)
actual fun sha512Hex(input: String): String {
    val data = input.encodeToByteArray()
    val digest = UByteArray(CC_SHA512_DIGEST_LENGTH)
    digest.usePinned { out ->
        if (data.isEmpty()) {
            CC_SHA512(null, 0.convert(), out.addressOf(0))
        } else {
            data.usePinned { src ->
                CC_SHA512(src.addressOf(0), data.size.convert(), out.addressOf(0))
            }
        }
    }
    return digest.joinToString("") { it.toString(16).padStart(2, '0') }
}
