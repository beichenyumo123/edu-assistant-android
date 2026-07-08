package com.zxxf.assistant.data.api

import com.zxxf.assistant.data.dto.ClearMemoryResponse
import com.zxxf.assistant.data.dto.MemoryResponse
import com.zxxf.assistant.data.dto.MemoryUpdateRequest
import retrofit2.http.*

interface MemoryApi {

    @GET("/api/memory/me")
    suspend fun getMemory(): MemoryResponse

    @PATCH("/api/memory/me")
    suspend fun updateMemory(@Body request: MemoryUpdateRequest): MemoryResponse

    @DELETE("/api/memory/me")
    suspend fun clearMemory(): ClearMemoryResponse
}
