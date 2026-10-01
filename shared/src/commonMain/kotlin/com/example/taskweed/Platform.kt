package com.example.taskweed

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform