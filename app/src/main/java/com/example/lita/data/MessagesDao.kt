package com.example.lita.data

import androidx.room.*
import com.example.lita.models.ChatMessage
import retrofit2.http.PUT

@Dao
interface MessagesDao {
    @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT :records OFFSET :offset")
    suspend fun get(offset: Int, records: Int): List<ChatMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(message: ChatMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAll(messages: List<ChatMessage>)

    @Update(entity = ChatMessage::class)
    suspend fun update(chatMessage: ChatMessage)

    @Delete(entity = ChatMessage::class)
    suspend fun delete(chatMessage: ChatMessage)

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun get(id: String): ChatMessage

}