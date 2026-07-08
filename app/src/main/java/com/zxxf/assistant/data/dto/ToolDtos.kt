package com.zxxf.assistant.data.dto

import com.google.gson.annotations.SerializedName

// ── Summarize ──

data class SummarizeRequest(
    @SerializedName("document_id") val documentId: Long,
    val length: String = "medium"   // "short", "medium", "long"
)

data class SummarizeResponse(
    val summary: String,
    @SerializedName("document_id") val documentId: Long
)

// ── Extract Knowledge ──

data class ExtractKnowledgeRequest(
    @SerializedName("document_id") val documentId: Long
)

data class ExtractKnowledgeResponse(
    @SerializedName("knowledge_points") val knowledgePoints: List<KnowledgePointDto>,
    @SerializedName("document_id") val documentId: Long
)

data class KnowledgePointDto(
    val category: String? = null,
    val title: String,
    val description: String,
    @SerializedName("key_points") val keyPoints: List<String>? = null,
    val examples: List<String>? = null,
    @SerializedName("source_excerpt") val sourceExcerpt: String? = null,
    @SerializedName("relevant_chunks") val relevantChunks: List<RelevantChunkDto>? = null
)

data class RelevantChunkDto(
    val text: String? = null,
    @SerializedName("chunk_index") val chunkIndex: Int? = null
)
