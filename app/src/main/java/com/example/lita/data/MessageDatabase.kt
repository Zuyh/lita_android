package com.example.lita.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.lita.models.ChatMessage

@Database(entities = [ChatMessage::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MessageDatabase: RoomDatabase() {
    abstract val dao: MessagesDao
}