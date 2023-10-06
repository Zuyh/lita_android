package com.example.lita.retrofit

import com.example.lita.models.*
import retrofit2.Response
import retrofit2.http.*
import java.util.*
import kotlin.collections.HashMap

interface HistoryApi {
    @GET("/Prod/history")
    suspend fun getMessages(@Query("last_timestamp") lastTimestamp: String?): Response<List<ChatMessage>>

    @POST("/Prod/history")
    suspend fun postMessage(@Body message: AddMessageRequest): Response<ChatMessage>

    @PATCH("/Prod/history")
    suspend fun patchMessage(@Body message: PatchMessageRequest): Response<ChatMessage>

    @HTTP(method = "DELETE", path = "/Prod/history", hasBody = true)
    suspend fun deleteMessage(@Body identifier: DeleteMessageRequest): Response<Unit>

    @POST("/Prod/token")
    suspend fun addToken(@Body token: AddTokenRequest): Response<Unit>
}