package com.dweb.std.core

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform