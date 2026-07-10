package com.zxxf.assistant.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.zxxf.assistant.R
import com.zxxf.assistant.AppContainer
import com.zxxf.assistant.ui.chat.components.*
import com.zxxf.assistant.ui.chat.memory.MemorySheet
import com.zxxf.assistant.ui.knowledge.KnowledgeSheet
import com.zxxf.assistant.ui.theme.Surface0
import com.zxxf.assistant.ui.theme.Surface1
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    appContainer: AppContainer,
    onNavigateToProfile: () -> Unit = {},
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
    var showKnowledgeSheet by remember { mutableStateOf(false) }
    var showMemorySheet by remember { mutableStateOf(false) }

    // Connect WebSocket on first launch
    LaunchedEffect(Unit) {
        try {
            val user = appContainer.authRepository.getCurrentUser()
            chatViewModel.connectWebSocket(user.user.id)
        } catch (e: Exception) {
            // If getCurrentUser fails, the AuthInterceptor will handle 401
            // Don't crash the effect — it would restart and cause flickering
        }
    }

    // Scroll instantly while streaming (no animation overhead per token)
    LaunchedEffect(uiState.streamingMessage?.content) {
        if (uiState.streamingMessage != null && uiState.messages.isNotEmpty()) {
            // scrollToItem: instant scroll — avoids per-token animateScrollToItem overhead
            // "streaming" item is right after all completed messages
            listState.scrollToItem(uiState.messages.size)
        }
    }

    // Smooth scroll on message completion (Done / conversation switch)
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty() && uiState.streamingMessage == null) {
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
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 328.dp)
            ) {
                ConversationBottomSheetContent(
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
                    },
                    onRefresh = { chatViewModel.loadConversations() }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
                                )
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_brand_logo),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "CorpKnow Compass",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "企业知识导航",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open()
                                else drawerState.close()
                            }
                        }) {
                            Icon(
                                Icons.Filled.Menu,
                                contentDescription = "对话列表",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        var menuExpanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = "更多",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.width(216.dp),
                            shape = RoundedCornerShape(22.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            shadowElevation = 10.dp,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
                            )
                        ) {
                            Spacer(modifier = Modifier.height(6.dp))
                            CompassMenuItem(
                                text = "个人资料",
                                icon = Icons.Filled.Person,
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToProfile()
                                }
                            )
                            CompassMenuItem(
                                text = "AI 记忆",
                                icon = Icons.Filled.Memory,
                                onClick = {
                                    menuExpanded = false
                                    showMemorySheet = true
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            )
                            CompassMenuItem(
                                text = "退出登录",
                                icon = Icons.AutoMirrored.Filled.Logout,
                                isDestructive = true,
                                onClick = {
                                    menuExpanded = false
                                    onLogout()
                                }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.78f)
                    ),
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        DocumentScopeBar(
                            totalDocumentCount = uiState.totalDocumentCount,
                            selectedDocCount = uiState.selectedDocCount,
                            onOpenKnowledgeSheet = { showKnowledgeSheet = true },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        InputBar(
                            isThinking = uiState.isThinking,
                            onSend = { text -> chatViewModel.sendMessage(text) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                val hasContent = uiState.messages.isNotEmpty()
                    || uiState.streamingMessage != null
                    || uiState.isThinking

                if (!hasContent) {
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
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Completed messages — stable keys, never recompose during streaming
                        itemsIndexed(
                            uiState.messages,
                            key = { index, msg -> msg.id ?: index.toLong() }
                        ) { _, message ->
                            MessageBubble(
                                message = message,
                                showDetails = message.role == "assistant"
                            )
                        }

                        // Live streaming message — separate item with stable key "streaming"
                        // Only this item recomposes on each token
                        val streaming = uiState.streamingMessage
                        if (streaming != null) {
                            item(key = "streaming") {
                                MessageBubble(
                                    message = streaming,
                                    showDetails = false  // panels only appear after Done
                                )
                            }
                        }

                        // Live thinking steps during streaming (before any tokens arrive)
                        if (uiState.isThinking
                            && uiState.streamingMessage?.content.isNullOrEmpty()
                            && uiState.thinkingSteps.isNotEmpty()
                        ) {
                            item(key = "live_thinking") {
                                ThinkingSteps(
                                    steps = null,
                                    liveSteps = uiState.thinkingSteps
                                )
                            }
                        }

                        // Thinking indicator (spinner when no tokens yet and no steps)
                        if (uiState.isThinking
                            && uiState.streamingMessage?.content.isNullOrEmpty()
                            && uiState.thinkingSteps.isEmpty()
                        ) {
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

    // KnowledgeSheet
    if (showKnowledgeSheet) {
        KnowledgeSheet(
            fileRepository = appContainer.fileRepository,
            toolRepository = appContainer.toolRepository,
            onDismiss = {
                showKnowledgeSheet = false
                chatViewModel.loadDocuments()
            },
            onAskQuestion = { question ->
                chatViewModel.sendMessage(question)
            }
        )
    }

    // MemorySheet
    if (showMemorySheet) {
        MemorySheet(
            memoryRepository = appContainer.memoryRepository,
            onDismiss = { showMemorySheet = false }
        )
    }
}

@Composable
private fun CompassMenuItem(
    text: String,
    icon: ImageVector,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val iconTint = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    val iconBackground = if (isDestructive) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.46f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f)
    }
    val textColor = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    DropdownMenuItem(
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = textColor
            )
        },
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .height(48.dp),
        leadingIcon = {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(11.dp),
                color = iconBackground
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = iconTint
                    )
                }
            }
        },
        colors = MenuDefaults.itemColors(
            textColor = textColor,
            leadingIconColor = iconTint
        )
    )
}

/**
 * A single message bubble. For user messages: plain text, right-aligned.
 * For assistant messages: Markdown when complete, plain text while streaming,
 * plus ThinkingSteps / SourcesPanel / EvaluationPanel below when available.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: MessageUiItem,
    showDetails: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val context = LocalContext.current

    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp)
    }

    val bubbleModifier = Modifier
        .shadow(
            elevation = if (isUser) 0.dp else 2.dp,
            shape = bubbleShape,
            spotColor = Color.Black.copy(alpha = 0.05f)
        )
        .widthIn(max = 348.dp)
        .combinedClickable(
            onClick = { },
            onLongClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("message", message.content))
                Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
            }
        )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surface
            },
            shape = bubbleShape,
            border = if (!isUser) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f))
            } else {
                null
            },
            modifier = bubbleModifier
        ) {
            if (isUser) {
                Text(
                    text = message.content.ifEmpty { "..." },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else if (message.isStreaming) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(17.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message.content.ifEmpty { "..." },
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
                    Markdown(
                        content = message.content.ifEmpty { "..." },
                        modifier = Modifier.fillMaxWidth(),
                        colors = markdownColor(codeBackground = Surface0),
                        components = catppuccinMarkdownComponents
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
