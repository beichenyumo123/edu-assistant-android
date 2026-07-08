package com.zxxf.assistant.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zxxf.assistant.AppContainer
import com.zxxf.assistant.data.dto.SourceDto
import com.zxxf.assistant.ui.chat.components.ConversationDrawerContent
import com.zxxf.assistant.ui.chat.components.InputBar
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

    // Connect WebSocket on first launch
    LaunchedEffect(Unit) {
        val user = appContainer.authRepository.getCurrentUser()
        chatViewModel.connectWebSocket(user.user.id)
    }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(uiState.messages.size, uiState.streamingContent) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // error shown inline for now
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
                InputBar(
                    isThinking = uiState.isThinking,
                    onSend = { text -> chatViewModel.sendMessage(text) }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (uiState.messages.isEmpty()) {
                    // Welcome screen
                    WelcomeContent(
                        onPresetClick = { question -> chatViewModel.sendMessage(question) }
                    )
                } else {
                    // Messages
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.messages) { message ->
                            MessageBubble(message = message)
                        }

                        // Thinking indicator
                        if (uiState.isThinking && uiState.streamingContent.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
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

@Composable
private fun WelcomeContent(onPresetClick: (String) -> Unit) {
    val presetQuestions = listOf(
        "入职第一周需要完成哪些事项？",
        "试用期转正评估主要看什么？",
        "请假和异常打卡应该怎么处理？",
        "差旅报销需要注意哪些要求？",
        "哪些公司数据不能外发或上传？",
        "帮我整理新人必修培训清单"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "你好，我是入职培训助手",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "基于已上传的企业培训资料，回答入职、制度、流程、安全等问题",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Preset question cards (2 columns)
        repeat(3) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(2) { col ->
                    val index = row * 2 + col
                    if (index < presetQuestions.size) {
                        AssistChip(
                            onClick = { onPresetClick(presetQuestions[index]) },
                            label = {
                                Text(
                                    text = presetQuestions[index],
                                    maxLines = 2
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: MessageUiItem) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = message.content.ifEmpty { "..." },
                modifier = Modifier.padding(12.dp),
                color = if (isUser) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
