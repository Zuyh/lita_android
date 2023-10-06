package com.example.lita.helpers

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Utilities {
    companion object {
        fun localDateTimeToString(ldt: LocalDateTime): String {
            val pattern: String
            val currentDate = LocalDate.now()
            val date = ldt.toLocalDate()
            pattern = if (currentDate.isEqual(date)) { "今日  HH:mm" }
            else if (currentDate.minusDays(1).isEqual(date)) { "昨日  HH:mm" }
            else if (currentDate.year == date.year) { "MM/dd  HH:mm" }
            else { "yyyy/MM/dd" }
            return ldt.format(DateTimeFormatter.ofPattern(pattern))
        }
    }
}