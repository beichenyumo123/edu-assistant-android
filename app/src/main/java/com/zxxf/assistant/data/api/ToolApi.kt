package com.zxxf.assistant.data.api

import com.zxxf.assistant.data.dto.ExtractKnowledgeRequest
import com.zxxf.assistant.data.dto.ExtractKnowledgeResponse
import com.zxxf.assistant.data.dto.SummarizeRequest
import com.zxxf.assistant.data.dto.SummarizeResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ToolApi {

    @POST("/api/tools/summarize")
    suspend fun summarize(@Body request: SummarizeRequest): SummarizeResponse

    @POST("/api/tools/extract-knowledge")
    suspend fun extractKnowledge(@Body request: ExtractKnowledgeRequest): ExtractKnowledgeResponse
}
