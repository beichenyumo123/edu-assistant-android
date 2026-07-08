package com.zxxf.assistant.ui.chat

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.markdown.material3.MarkdownDocument
import com.zxxf.assistant.AppContainer
import com.zxxf.assistant.ui.chat.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    appContainer: AppContainer,
    onLogout: () -> Unit
) {
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.Factory(
            appContainer.chatRepository,
            appContainer.conversationRepository,
            appContainer.fileRepository,
            appContainer.toolRepository,
            appContainer.memoryRepository
        )
    )

    val uiState by chatViewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Connect WebSocket on first launch
    LaunchedEffect(Unit) {
        val user = appContainer.authRepository.getCurrentUser()
        chatViewModel.connectWebSocket(user.user.id)
    }

    // Auto-scroll to bottom on new messages or streaming content
    LaunchedEffect(uiState.messages.size, uiState.streamingContent) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            chatViewModel.clearError()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                ConversationDrawerContent(
                    conversations = uiState.conversations,
                    currentConversationId = uiState.currentConversationId,
                    onNewConversation = {
                        chatViewModel.newConversation()
                        scope.launch { drawerState.close() }
                    },
                    onSelectConversation = { id ->
                        chatViewModel.selectConversation(id)
                        scope.launch { drawerState.close() }
                    },
                    onDeleteConversation = { id ->
                        chatViewModel.deleteConversation(id)
                    }
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("OnboardAgent") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open()
                                else drawerState.close()
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "对话列表")
                        }
                    },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "退出登录"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                Column {
                    // Document scope indicator above input
                    DocumentScopeBar(
                        totalDocumentCount = uiState.totalDocumentCount,
                        selectedDocCount = uiState.selectedDocCount,
                        onOpenKnowledgeSheet = {
                            // TODO: Open KnowledgeSheet (Block 2)
                        }
                    )
                    InputBar(
                        isThinking = uiState.isThinking,
                        onSend = { text -> chatViewModel.sendMessage(text) }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (uiState.messages.isEmpty() && !uiState.isThinking) {
                    // Welcome screen
                    WelcomeCards(
                        onPresetClick = { question -> chatViewModel.sendMessage(question) }
                    )
                } else {
                    // Messages list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            uiState.messages,
                            key = { index, _ -> index }
                        ) { index, message ->
                            MessageBubble(
                                message = message,
                                showDetails = message.role == "assistant" && !message.isStreaming
                            )
                        }

                        // Live thinking steps during streaming (before any tokens arrive)
                        if (uiState.isThinking && uiState.streamingContent.isEmpty() && uiState.thinkingSteps.isNotEmpty()) {
                            item(key = "live_thinking") {
                                ThinkingSteps(
                                    steps = null,
                                    liveSteps = uiState.thinkingSteps
                                )
                            }
                        }

                        // Thinking indicator (spinner when no tokens yet and no steps)
                        if (uiState.isThinking && uiState.streamingContent.isEmpty() && uiState.thinkingSteps.isEmpty()) {
                            item(key = "thinking_indicator") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "正在思考...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A single message bubble. For user messages: plain text, right-aligned.
 * For assistant messages: Markdown when complete, plain text while streaming,
 * plus ThinkingSteps / SourcesPanel / EvaluationPanel below when available.
 */
@Composable
fun MessageBubble(
    message: MessageUiItem,
    showDetails: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Content bubble
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            if (isUser) {
                // User messages: plain text only
                Text(
                    text = message.content.ifEmpty { "..." },
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else if (message.isStreaming) {
                // Streaming: plain text (avoid Markdown re-render flicker)
                Text(
                    text = message.content.ifEmpty { "..." },
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                // Completed assistant message: render Markdown
                Column(modifier = Modifier.padding(12.dp)) {
                    MarkdownDocument(
                        markdown = message.content.ifEmpty { "..." },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Details panels (only for completed assistant messages)
        if (showDetails) {
            Spacer(modifier = Modifier.height(6.dp))

            // Thinking steps
            ThinkingSteps(
                steps = message.agentSteps,
                modifier = Modifier.widthIn(max = 340.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Source citations
            SourcesPanel(
                sources = message.sources,
                modifier = Modifier.widthIn(max = 340.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // RAG evaluation
            EvaluationPanel(
                evaluation = message.evaluation,
                modifier = Modifier.widthIn(max = 340.dp)
            )
        }
    }
}
