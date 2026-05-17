package com.resthalflab.resthalfapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform