package com.zxxf.assistant.data.dto

import com.google.gson.annotations.SerializedName

// ── GET /api/memory/me ──

data class MemoryResponse(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("memory_enabled") val memoryEnabled: Boolean,
    val grade: String? = null,
    val major: String? = null,
    @SerializedName("question_count") val questionCount: Int = 0,
    @SerializedName("preferred_answer_style") val preferredAnswerStyle: String? = null,
    @SerializedName("communication_tone") val communicationTone: String? = null,
    @SerializedName("top_topics") val topTopics: Map<String, Int>? = null,
    @SerializedName("document_preferences") val documentPreferences: Map<String, Int>? = null,
    @SerializedName("last_question") val lastQuestion: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

// ── PATCH /api/memory/me ──

data class MemoryUpdateRequest(
    @SerializedName("memory_enabled") val memoryEnabled: Boolean? = null,
    @SerializedName("preferred_answer_style") val preferredAnswerStyle: String? = null,
    @SerializedName("communication_tone") val communicationTone: String? = null
)

// ── DELETE /api/memory/me ──

data class ClearMemoryResponse(
    val success: Boolean,
    val message: String
)
