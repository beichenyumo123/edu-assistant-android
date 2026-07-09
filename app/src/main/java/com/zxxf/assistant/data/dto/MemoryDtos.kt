package com.zxxf.assistant.data.dto

import com.google.gson.annotations.SerializedName

// ── GET /api/memory/me ──

data class MemoryResponse(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("memory_enabled") val memoryEnabled: Boolean,
    @SerializedName("department") val department: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("question_count") val questionCount: Int = 0,
    @SerializedName("preferred_answer_style") val preferredAnswerStyle: String? = null,
    @SerializedName("communication_tone") val communicationTone: String? = null,
    @SerializedName("top_topics") val topTopics: List<TopicItem>? = null,
    @SerializedName("document_preferences") val documentPreferences: List<TopicItem>? = null,
    @SerializedName("last_question") val lastQuestion: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class TopicItem(
    val name: String,
    val count: Int
)

// ── PATCH /api/memory/me ──

data class MemoryUpdateRequest(
    @SerializedName("memory_enabled") val memoryEnabled: Boolean? = null,
    @SerializedName("preferred_answer_style") val preferredAnswerStyle: String? = null,
    @SerializedName("communication_tone") val communicationTone: String? = null
)

// ── DELETE /api/memory/me & PATCH /api/memory/me (both return { memory: ... }) ──

data class MemoryWrapper(
    val memory: MemoryResponse
)
