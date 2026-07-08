package com.zxxf.assistant.data.repository

import com.zxxf.assistant.data.api.ToolApi
import com.zxxf.assistant.data.dto.ExtractKnowledgeRequest
import com.zxxf.assistant.data.dto.ExtractKnowledgeResponse
import com.zxxf.assistant.data.dto.SummarizeRequest
import com.zxxf.assistant.data.dto.SummarizeResponse

class ToolRepository(private val toolApi: ToolApi) {

    suspend fun summarize(documentId: Long, length: String = "medium"): SummarizeResponse {
        return toolApi.summarize(SummarizeRequest(documentId, length))
    }

    suspend fun extractKnowledge(documentId: Long): ExtractKnowledgeResponse {
        return toolApi.extractKnowledge(ExtractKnowledgeRequest(documentId))
    }
}
