package com.example.lita.data

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class Converters {
    @TypeConverter
    fun fromString(value: String): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss.SSSSSS")
        return LocalDateTime.parse(value, formatter)
    }

    @TypeConverter
    fun toString(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss.SSSSSS")
        return dateTime.format(formatter)
    }
}