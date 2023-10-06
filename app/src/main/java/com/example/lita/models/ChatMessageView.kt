package com.example.lita.models

import java.time.LocalDateTime

data class ChatMessageView(
    val id: String,
    val sender: String,
    val message: String,
    val isHistoryEnabled: Boolean,
    val timestamp: LocalDateTime
)

