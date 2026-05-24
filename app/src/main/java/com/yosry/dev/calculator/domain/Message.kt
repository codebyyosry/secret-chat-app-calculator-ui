package com.yosry.dev.calculator.domain

// Message.kt
data class Message(
    val id: String = "",
    val senderCode: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)