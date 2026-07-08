package com.zxxf.assistant.data.repository

import com.zxxf.assistant.data.api.MemoryApi
import com.zxxf.assistant.data.dto.MemoryResponse
import com.zxxf.assistant.data.dto.MemoryUpdateRequest

class MemoryRepository(private val memoryApiProvider: () -> MemoryApi) {

    private val memoryApi: MemoryApi get() = memoryApiProvider()

    suspend fun getMemory(): MemoryResponse {
        return memoryApi.getMemory()
    }

    suspend fun updateMemory(
        memoryEnabled: Boolean? = null,
        preferredAnswerStyle: String? = null,
        communicationTone: String? = null
    ): MemoryResponse {
        return memoryApi.updateMemory(
            MemoryUpdateRequest(memoryEnabled, preferredAnswerStyle, communicationTone)
        )
    }

    suspend fun clearMemory() {
        memoryApi.clearMemory()
    }
}
