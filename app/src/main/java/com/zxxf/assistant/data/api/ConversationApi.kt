package com.zxxf.assistant.data.api

import com.zxxf.assistant.data.dto.ConversationDetailResponse
import com.zxxf.assistant.data.dto.ConversationListResponse
import com.zxxf.assistant.data.dto.DeleteConversationResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface ConversationApi {

    @GET("/api/conversations")
    suspend fun list(): ConversationListResponse

    @GET("/api/conversations/{id}")
    suspend fun get(@Path("id") conversationId: Long): ConversationDetailResponse

    @DELETE("/api/conversations/{id}")
    suspend fun delete(@Path("id") conversationId: Long): DeleteConversationResponse
}
