package com.zxxf.assistant.data.api

import com.zxxf.assistant.data.dto.MemoryResponse
import com.zxxf.assistant.data.dto.MemoryUpdateRequest
import com.zxxf.assistant.data.dto.MemoryWrapper
import retrofit2.http.*

interface MemoryApi {

    @GET("/api/memory/me")
    suspend fun getMemory(): MemoryWrapper

    @PATCH("/api/memory/me")
    suspend fun updateMemory(@Body request: MemoryUpdateRequest): MemoryWrapper

    @DELETE("/api/memory/me")
    suspend fun clearMemory(): MemoryWrapper
}
