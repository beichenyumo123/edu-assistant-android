package com.zxxf.assistant.data.repository

import com.zxxf.assistant.data.api.ChatApi
import com.zxxf.assistant.data.dto.ChatRequest
import com.zxxf.assistant.data.dto.ChatResponse
import com.zxxf.assistant.data.websocket.ChatWebSocket
import com.zxxf.assistant.data.websocket.WsMessage
import com.zxxf.assistant.util.TokenManager
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val chatApiProvider: () -> ChatApi,
    private val baseUrlProvider: () -> String,
    private val tokenManager: TokenManager
) {
    private var webSocket: ChatWebSocket? = null

    private val chatApi: ChatApi get() = chatApiProvider()
    private val baseUrl: String get() = baseUrlProvider()

    /**
     * Connect WebSocket for streaming chat. Call once after login.
     * Uses the current baseUrl from ServerConfig each time.
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
