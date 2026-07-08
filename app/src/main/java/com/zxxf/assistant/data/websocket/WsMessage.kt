package com.zxxf.assistant.data.websocket

import com.zxxf.assistant.data.dto.AgentStepDto
import com.zxxf.assistant.data.dto.EvaluationDto
import com.zxxf.assistant.data.dto.SourceDto
import com.google.gson.annotations.SerializedName

sealed class WsMessage {
    data class Meta(
        @SerializedName("conversation_id") val conversationId: Long
    ) : WsMessage()

    data class Thinking(
        val step: String? = null,
        val text: String? = null,
        @SerializedName("tool_name") val toolName: String? = null,
        @SerializedName("elapsed_ms") val elapsedMs: Long? = null
    ) : WsMessage()

    data class Token(val content: String) : WsMessage()

    data class Done(
        val sources: List<SourceDto>? = null,
        @SerializedName("agent_steps") val agentSteps: List<AgentStepDto>? = null,
        val evaluation: EvaluationDto? = null
    ) : WsMessage()

    data class Error(val message: String) : WsMessage()
}

// Raw incoming WS JSON wrapper for deserialization
data class WsRawMessage(
    val type: String,
    @SerializedName("conversation_id") val conversationId: Long? = null,
    val step: String? = null,
    val text: String? = null,
    @SerializedName("tool_name") val toolName: String? = null,
    @SerializedName("elapsed_ms") val elapsedMs: Long? = null,
    val content: String? = null,
    val sources: List<SourceDto>? = null,
    @SerializedName("agent_steps") val agentSteps: List<AgentStepDto>? = null,
    val evaluation: EvaluationDto? = null,
    val message: String? = null
) {
    fun toWsMessage(): WsMessage = when (type) {
        "meta" -> WsMessage.Meta(conversationId ?: 0L)
        "thinking" -> WsMessage.Thinking(
            step = step ?: text,
            text = text ?: step,
            toolName = toolName,
            elapsedMs = elapsedMs
        )
        "token" -> WsMessage.Token(content ?: "")
        "done" -> WsMessage.Done(sources, agentSteps, evaluation)
        "error" -> WsMessage.Error(message ?: "未知错误")
        else -> WsMessage.Error("未知消息类型: $type")
    }
}
