package com.zxxf.assistant.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zxxf.assistant.data.dto.*
import com.zxxf.assistant.data.repository.*
import com.zxxf.assistant.data.websocket.WsMessage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatUiState(
    val conversations: List<ConversationDto> = emptyList(),
    val currentConversationId: Long? = null,
    val messages: List<MessageUiItem> = emptyList(),
    val isThinking: Boolean = false,
    val thinkingSteps: List<AgentStepMsg> = emptyList(),
    val streamingMessage: MessageUiItem? = null,
    val error: String? = null,
    val totalDocumentCount: Int = 0,
    val selectedDocCount: Int = 0
)

data class MessageUiItem(
    val id: Long? = null,
    val role: String,  // "user" or "assistant"
    val content: String,
    val sources: List<SourceDto>? = null,
    val agentSteps: List<AgentStepDto>? = null,
    val evaluation: EvaluationDto? = null,
    val isStreaming: Boolean = false  // true while tokens are still arriving
)

data class AgentStepMsg(
    val text: String,
    val toolName: String? = null,
    val elapsedMs: Long? = null
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val conversationRepository: ConversationRepository,
    private val fileRepository: FileRepository,
    private val toolRepository: ToolRepository,
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // Knowledge base
    private val _documents = MutableStateFlow<List<DocumentDto>>(emptyList())
    val documents: StateFlow<List<DocumentDto>> = _documents.asStateFlow()

    private val _selectedDocIds = MutableStateFlow<List<Long>>(emptyList())
    val selectedDocIds: StateFlow<List<Long>> = _selectedDocIds.asStateFlow()

    init {
        loadConversations()
        loadDocuments()
    }

    fun connectWebSocket(userId: Int) {
        viewModelScope.launch {
            chatRepository.connectWebSocket(userId).conflate().collect { wsMessage ->
                handleWsMessage(wsMessage)
            }
        }
    }

    fun disconnectWebSocket() {
        chatRepository.disconnectWebSocket()
    }

    // ── Conversation management ──

    fun loadConversations() {
        viewModelScope.launch {
            try {
                val response = conversationRepository.list()
                _uiState.update { it.copy(conversations = response.conversations) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "加载对话列表失败") }
            }
        }
    }

    fun selectConversation(conversationId: Long) {
        viewModelScope.launch {
            try {
                val response = conversationRepository.get(conversationId)
                val messages = response.messages.mapIndexed { idx, msg ->
                    MessageUiItem(
                        id = idx.toLong(),
                        role = msg.role,
                        content = msg.content,
                        sources = msg.sources,
                        agentSteps = msg.agentSteps,
                        evaluation = msg.evaluation
                    )
                }
                _uiState.update {
                    it.copy(
                        currentConversationId = conversationId,
                        messages = messages
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "加载对话失败") }
            }
        }
    }

    fun deleteConversation(conversationId: Long) {
        viewModelScope.launch {
            try {
                conversationRepository.delete(conversationId)
                if (_uiState.value.currentConversationId == conversationId) {
                    newConversation()
                }
                loadConversations()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "删除对话失败") }
            }
        }
    }

    fun newConversation() {
        _uiState.update {
            it.copy(
                currentConversationId = null,
                messages = emptyList(),
                thinkingSteps = emptyList(),
                streamingMessage = null,
                isThinking = false
            )
        }
    }

    // ── Send message ──

    fun sendMessage(text: String) {
        val convId = _uiState.value.currentConversationId
        val docIds = _selectedDocIds.value.ifEmpty { null }

        // Add user message
        val userMsg = MessageUiItem(role = "user", content = text)
        // Start streaming placeholder (separate from completed messages — O(1) per token)
        val streamingMsg = MessageUiItem(role = "assistant", content = "", isStreaming = true)

        _uiState.update {
            it.copy(
                messages = it.messages + userMsg,
                streamingMessage = streamingMsg,
                isThinking = true,
                thinkingSteps = emptyList(),
                error = null
            )
        }

        viewModelScope.launch {
            // Try WebSocket first
            val wsSent = chatRepository.sendViaWebSocket(text, convId, docIds)

            if (!wsSent) {
                // HTTP fallback
                try {
                    val response = chatRepository.sendViaHttp(text, convId, docIds)
                    val completed = MessageUiItem(
                        role = "assistant",
                        content = response.message.content,
                        sources = response.message.sources,
                        agentSteps = response.agentSteps,
                        evaluation = response.evaluation,
                        isStreaming = false
                    )
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + completed,
                            streamingMessage = null,
                            isThinking = false,
                            currentConversationId = response.conversationId
                        )
                    }
                    loadConversations()
                } catch (e: Exception) {
                    _uiState.update { state ->
                        state.copy(
                            streamingMessage = null,
                            isThinking = false,
                            error = e.message ?: "消息发送失败"
                        )
                    }
                }
            }
        }
    }

    // ── WebSocket message handler ──

    private fun handleWsMessage(msg: WsMessage) {
        when (msg) {
            is WsMessage.Meta -> {
                _uiState.update {
                    if (it.currentConversationId == null && msg.conversationId > 0) {
                        it.copy(currentConversationId = msg.conversationId)
                    } else it
                }
            }
            is WsMessage.Thinking -> {
                val step = AgentStepMsg(
                    text = msg.text ?: msg.step ?: "",
                    toolName = msg.toolName,
                    elapsedMs = msg.elapsedMs
                )
                _uiState.update {
                    it.copy(thinkingSteps = it.thinkingSteps + step)
                }
            }
            is WsMessage.Token -> {
                _uiState.update { state ->
                    val current = state.streamingMessage ?: return@update state
                    val updated = current.copy(
                        content = current.content + msg.content,
                        isStreaming = true
                    )
                    state.copy(streamingMessage = updated)
                }
            }
            is WsMessage.Done -> {
                _uiState.update { state ->
                    val completed = (state.streamingMessage ?: return@update state).copy(
                        sources = msg.sources,
                        agentSteps = msg.agentSteps,
                        evaluation = msg.evaluation,
                        isStreaming = false
                    )
                    state.copy(
                        messages = state.messages + completed,
                        streamingMessage = null,
                        isThinking = false,
                        thinkingSteps = emptyList()
                    )
                }
                loadConversations()
            }
            is WsMessage.Error -> {
                _uiState.update { state ->
                    val failed = state.streamingMessage?.copy(isStreaming = false)
                    state.copy(
                        messages = if (failed != null) state.messages + failed else state.messages,
                        streamingMessage = null,
                        isThinking = false,
                        error = msg.message
                    )
                }
            }
        }
    }

    // ── Knowledge base ──

    fun loadDocuments() {
        viewModelScope.launch {
            try {
                val response = fileRepository.list()
                _documents.value = response.files
                _uiState.update {
                    it.copy(totalDocumentCount = response.files.size)
                }
            } catch (_: Exception) { }
        }
    }

    fun toggleDocumentSelection(docId: Long) {
        _selectedDocIds.update { ids ->
            val new = if (ids.contains(docId)) ids - docId else ids + docId
            _uiState.update { it.copy(selectedDocCount = new.size) }
            new
        }
    }

    fun selectAllDocuments() {
        val all = _documents.value
            .filter { it.status == "ready" }
            .map { it.id }
        _selectedDocIds.value = all
        _uiState.update { it.copy(selectedDocCount = all.size) }
    }

    fun clearDocumentSelection() {
        _selectedDocIds.value = emptyList()
        _uiState.update { it.copy(selectedDocCount = 0) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    class Factory(
        private val chatRepository: ChatRepository,
        private val conversationRepository: ConversationRepository,
        private val fileRepository: FileRepository,
        private val toolRepository: ToolRepository,
        private val memoryRepository: MemoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(
                chatRepository, conversationRepository, fileRepository,
                toolRepository, memoryRepository
            ) as T
        }
    }
}
