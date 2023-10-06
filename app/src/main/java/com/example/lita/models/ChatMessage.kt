package com.example.lita.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "messages")
data class ChatMessage(
    @PrimaryKey
    val id: String = "",
    val senderId: Int,
    val originalMessage: String,
    val revisedMessage: String = "",
    val isHistoryEnabled: Boolean,
    val timestamp: String
)

fun ChatMessage.toChatMessageView(): ChatMessageView{
    val sender: String = Senders.fromId(senderId).toString()
    val message = revisedMessage.ifEmpty { originalMessage }
    val ldtTimestamp = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss.SSSSSS"))
    return ChatMessageView(
        id = id,
        sender = sender,
        message = message,
        isHistoryEnabled = isHistoryEnabled,
        timestamp = ldtTimestamp
    )
}

enum class Senders(val id: Int) {
        ME(0),
        WILLIS(1),
        SYSTEM(2);

    companion object {
        val map = values().associateBy(Senders::name)

        fun fromId(id: Int): Senders? {
            return values().find {it.id == id}
        }

        fun fromName(name: String): Senders? {
            return map[name]
        }
    }
}