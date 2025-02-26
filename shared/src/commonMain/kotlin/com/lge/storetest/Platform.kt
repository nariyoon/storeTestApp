package com.lge.storetest

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform