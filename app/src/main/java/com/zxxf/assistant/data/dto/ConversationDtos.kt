package com.zxxf.assistant.data.dto

import com.google.gson.annotations.SerializedName

// ── List ──

data class ConversationListResponse(
    val conversations: List<ConversationDto>
)

// ── Detail ──

data class ConversationDetailResponse(
    val conversation: ConversationDto,
    val messages: List<MessageDto>
)

data class ConversationDto(
    val id: Long,
    @SerializedName("user_id") val userId: Int,
    val title: String,
    @SerializedName("agent_type") val agentType: String? = null,
    @SerializedName("message_count") val messageCount: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

// ── Delete ──

data class DeleteConversationResponse(
    val success: Boolean,
    val message: String
)
