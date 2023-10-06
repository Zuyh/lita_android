package com.example.lita.models

data class AddMessageRequest (
    val message: String,
    val senderId: String,
    val isHistoryEnabled: String,
    val isGenerateResponseEnabled: String
)

data class DeleteMessageRequest (
    val id: String
)

data class PatchMessageRequest(
    val id: String,
    val senderId: String,
    val message: String,
    val isHistoryEnabled: String,
    val isGenerateResponseEnabled: String
)

data class AddTokenRequest(
    val token: String
)