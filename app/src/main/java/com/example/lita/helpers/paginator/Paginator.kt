package com.example.lita.helpers.paginator

interface Paginator<Key, Item> {
    suspend fun loadNextItem()
    fun reset()
}