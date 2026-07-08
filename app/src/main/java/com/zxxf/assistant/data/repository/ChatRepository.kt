package com.zxxf.assistant.data.repository

import com.zxxf.assistant.data.api.ChatApi
import com.zxxf.assistant.data.dto.ChatRequest
import com.zxxf.assistant.data.dto.ChatResponse
import com.zxxf.assistant.data.websocket.ChatWebSocket
import com.zxxf.assistant.data.websocket.WsMessage
import com.zxxf.assistant.util.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class ChatRepository(
    private val chatApi: ChatApi,
    private val baseUrl: String,
    private val tokenManager: TokenManager
) {
    private var webSocket: ChatWebSocket? = null

    /**
     * Connect WebSocket for streaming chat. Call once after login.
     */
    fun connectWebSocket(userId: Int): Flow<WsMessage> {
        webSocket?.disconnect()
        val ws = ChatWebSocket(userId, baseUrl)
        ws.connect()
        webSocket = ws
        return ws.messages
    }

    fun disconnectWebSocket() {
        webSocket?.disconnect()
        webSocket = null
    }

    fun sendViaWebSocket(
        message: String,
        conversationId: Long? = null,
        selectedDocIds: List<Long>? = null
    ): Boolean {
        return webSocket?.send(message, conversationId, selectedDocIds) ?: false
    }

    val isWebSocketConnected: Boolean get() = webSocket?.isConnected == true

    /**
     * HTTP fallback (synchronous, non-streaming)
     */
    suspend fun sendViaHttp(
        message: String,
        conversationId: Long? = null,
        selectedDocIds: List<Long>? = null
    ): ChatResponse {
        return chatApi.ask(
            ChatRequest(
                conversationId = conversationId,
                message = message,
                selectedDocumentIds = selectedDocIds
            )
        )
    }
}
