package com.example.lita

import com.example.lita.data.MessagesDao
import com.example.lita.models.*
import com.example.lita.retrofit.HistoryApi
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject.Inject

class MainPageRepository @Inject constructor(
    private val messagesDao: MessagesDao,
    private val historyApi: HistoryApi
) {

    suspend fun getMessages(startIndex: Int, records: Int): List<ChatMessage> {
        val data = messagesDao.get(offset = startIndex, records = records)
        return data
    }

    suspend fun sendMessage(textInput: String, sender: Senders, isHistoryEnabled: Boolean, isGenerateResponseEnabled: Boolean = true): ChatMessage {
        // Save message in remote server
        val response = historyApi.postMessage(AddMessageRequest(
            message = textInput,
            senderId = sender.id.toString(),
            isHistoryEnabled = isHistoryEnabled.toString(),
            isGenerateResponseEnabled = isGenerateResponseEnabled.toString())
        )
        if (!response.isSuccessful || response.body() == null) {
            throw Exception("Unknown Error from Retrofit")
        }

        // Update local database
        val newMessage = response.body()!!
        messagesDao.add(newMessage)
        return newMessage
    }

    suspend fun updateMessage(id: String, sender: Senders, newMessage: String, isHistoryEnabled: Boolean, isGenerateResponseEnabled: Boolean): ChatMessage {
        // Update remote server
        val response = historyApi.patchMessage(PatchMessageRequest(
            id = id,
            senderId = sender.id.toString(),
            message = newMessage,
            isHistoryEnabled = isHistoryEnabled.toString(),
            isGenerateResponseEnabled = isGenerateResponseEnabled.toString())
        )
        if (!response.isSuccessful || response.body() == null) {
            throw Exception("Unknown Error from Retrofit")
        }

        val data = response.body()!!
        // Save in the local database
        messagesDao.update(data)
        return data
    }

    suspend fun deleteMessage(id: String, timestamp: String) {
        val response = historyApi.deleteMessage(DeleteMessageRequest(id = id))

        if (!response.isSuccessful) {
            throw Exception("Unknown Error from Retrofit")
        }

        // Delete message from local database
        val oldMessage = messagesDao.get(id)
        messagesDao.delete(oldMessage)
        return
    }

    suspend fun getMessagesFromServer(lastTimestamp: String? = null) {
        val response = historyApi.getMessages(lastTimestamp)
        if (!response.isSuccessful) {
            throw Exception("Unknown Error from Retrofit")
        }

        if (response.body() != null) {
            val data = response.body()!!
            messagesDao.addAll(data)
        }
    }
}