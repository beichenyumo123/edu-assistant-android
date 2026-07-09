package com.zxxf.assistant.data.websocket

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.*

class ChatWebSocket(
    private val userId: Int,
    private val baseUrl: String
) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var reconnectAttempt = 0
    private var maxReconnect = 5
    private var shouldReconnect = true
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _messages = Channel<WsMessage>(Channel.BUFFERED)
    val messages: Flow<WsMessage> = _messages.receiveAsFlow()

    private val gson = Gson()

    data class WsSendPayload(
        val message: String,
        val conversation_id: Long? = null,
        val agent_type: String = "edu",
        val selected_document_ids: List<Long>? = null
    )

    fun connect() {
        val wsUrl = buildWsUrl()
        Log.d(TAG, "Connecting to WebSocket: $wsUrl")

        val request = Request.Builder()
            .url(wsUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected")
                reconnectAttempt = 0
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val raw = gson.fromJson(text, WsRawMessage::class.java)
                    val msg = raw.toWsMessage()
                    _messages.trySend(msg)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse WS message: $text", e)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code $reason")
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code $reason")
                attemptReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure", t)
                attemptReconnect()
            }
        })
    }

    fun disconnect() {
        shouldReconnect = false
        maxReconnect = 0
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        scope.cancel()
        _messages.close()
    }

    fun send(
        message: String,
        conversationId: Long? = null,
        selectedDocIds: List<Long>? = null
    ): Boolean {
        val ws = webSocket ?: return false
        val payload = WsSendPayload(
            message = message,
            conversation_id = conversationId,
            selected_document_ids = selectedDocIds
        )
        val json = gson.toJson(payload)
        return ws.send(json)
    }

    val isConnected: Boolean get() = webSocket != null

    private fun attemptReconnect() {
        if (!shouldReconnect || reconnectAttempt >= maxReconnect) return
        reconnectAttempt++
        val delayMs = 2000L * reconnectAttempt
        Log.d(TAG, "Reconnecting in ${delayMs}ms (attempt $reconnectAttempt/$maxReconnect)")
        scope.launch {
            delay(delayMs)
            connect()
        }
    }

    private fun buildWsUrl(): String {
        val httpUrl = baseUrl.trimEnd('/')
        val wsBase = if (httpUrl.startsWith("https://")) {
            httpUrl.replace("https://", "wss://")
        } else {
            httpUrl.replace("http://", "ws://")
        }
        return "$wsBase/ws/chat/$userId"
    }

    companion object {
        private const val TAG = "ChatWebSocket"
    }
}
