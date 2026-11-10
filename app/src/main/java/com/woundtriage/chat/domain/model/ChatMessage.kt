package com.woundtriage.chat.domain.model

import java.util.UUID

enum class MessageSender {
    USER,
    ASSISTANT,
    SYSTEM,
    EMERGENCY
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val dimension: SinbadDimension? = null,
    val isStreaming: Boolean = false
)
