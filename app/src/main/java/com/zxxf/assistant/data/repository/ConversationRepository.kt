package com.zxxf.assistant.data.repository

import com.zxxf.assistant.data.api.ConversationApi
import com.zxxf.assistant.data.dto.ConversationDetailResponse
import com.zxxf.assistant.data.dto.ConversationListResponse

class ConversationRepository(private val conversationApiProvider: () -> ConversationApi) {

    private val conversationApi: ConversationApi get() = conversationApiProvider()

    suspend fun list(): ConversationListResponse {
        return conversationApi.list()
    }

    suspend fun get(conversationId: Long): ConversationDetailResponse {
        return conversationApi.get(conversationId)
    }

    suspend fun delete(conversationId: Long) {
        conversationApi.delete(conversationId)
    }
}
