package com.zxxf.assistant.data.api

import com.zxxf.assistant.data.dto.ChatRequest
import com.zxxf.assistant.data.dto.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApi {

    @POST("/api/chat/ask")
    suspend fun ask(@Body request: ChatRequest): ChatResponse
}
