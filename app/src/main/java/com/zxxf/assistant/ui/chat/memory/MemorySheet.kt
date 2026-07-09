package com.zxxf.assistant.ui.chat.memory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zxxf.assistant.data.repository.MemoryRepository

// Must match backend validation: user_memory.py ANSWER_STYLE_OPTIONS
private val answerStyleOptions = listOf("结构化", "简洁", "详细", "步骤化", "表格化")

// Must match backend validation: user_memory.py COMMUNICATION_TONE_OPTIONS
private val communicationToneOptions = listOf("专业清晰", "直接高效", "耐心详细", "结构清晰")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemorySheet(
    memoryRepository: MemoryRepository,
    refreshKey: Int = 0,
    onDismiss: () -> Unit
) {
    val viewModel: MemoryViewModel = viewModel(
        factory = MemoryViewModel.Factory(memoryRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    // Reload memory data every time the sheet opens
    LaunchedEffect(Unit) {
        viewModel.loadMemory()
    }

    // Reload when conversation completes (refreshKey bumped in ChatScreen)
    LaunchedEffect(refreshKey) {
        if (refreshKey > 0) {
            viewModel.loadMemory()
        }
    }

    // Local edit state (synced from API data)
    var memoryEnabled by remember { mutableStateOf(true) }
    var answerStyle by remember { mutableStateOf("结构化") }
    var communicationTone by remember { mutableStateOf("专业清晰") }
    var answerStyleExpanded by remember { mutableStateOf(false) }
    var toneExpanded by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    // Populate from API response on first load / refresh
    LaunchedEffect(uiState.memory) {
        uiState.memory?.let { mem ->
            memoryEnabled = mem.memoryEnabled
            mem.preferredAnswerStyle?.let { answerStyle = it }
            mem.communicationTone?.let { communicationTone = it }
            initialized = true
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("AI记忆设置已保存", duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    // Clear confirmation dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("清空 AI 记忆") },
            text = { Text("确定清空所有 AI 记忆数据？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearMemory()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("清空") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("取消") }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.fillMaxHeight(0.88f)
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("AI 记忆") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "关闭")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                } else {
                    // ── Hero section (matches web) ──
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Personalized Context",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "个性化使用画像",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    // ── Stats row (matches web) ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            icon = Icons.Filled.ChatBubbleOutline,
                            label = "记录问题",
                            value = "${uiState.memory?.questionCount ?: 0}"
                        )
                        StatItem(
                            icon = Icons.Filled.Memory,
                            label = "记忆状态",
                            value = if (memoryEnabled) "已开启" else "已关闭",
                            valueColor = if (memoryEnabled) Color(0xFF34A853)
                            else MaterialTheme.colorScheme.outline
                        )
                    }

                    HorizontalDivider()

                    // ── Settings section (matches web "用户可控设置") ──
                    Text(
                        text = "用户可控设置",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    // Memory toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "启用 AI 记忆",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = "开启后系统会记录稳定偏好并用于后续回答",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(checked = memoryEnabled, onCheckedChange = { memoryEnabled = it })
                    }

                    // Answer style (matches web options)
                    ExposedDropdownMenuBox(
                        expanded = answerStyleExpanded,
                        onExpandedChange = { answerStyleExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = answerStyle,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("回答风格") },
                            enabled = memoryEnabled,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = answerStyleExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = answerStyleExpanded,
                            onDismissRequest = { answerStyleExpanded = false }
                        ) {
                            answerStyleOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        answerStyle = option
                                        answerStyleExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Communication tone (matches web options)
                    ExposedDropdownMenuBox(
                        expanded = toneExpanded,
                        onExpandedChange = { toneExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = communicationTone,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("沟通语气") },
                            enabled = memoryEnabled,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toneExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = toneExpanded,
                            onDismissRequest = { toneExpanded = false }
                        ) {
                            communicationToneOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        communicationTone = option
                                        toneExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Save button
                    Button(
                        onClick = {
                            viewModel.saveSettings(
                                memoryEnabled = memoryEnabled,
                                preferredAnswerStyle = answerStyle,
                                communicationTone = communicationTone
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("保存记忆设置")
                    }

                    // ── Profile section (matches web "基础画像") ──
                    uiState.memory?.let { mem ->
                        val hasProfile = mem.department != null || mem.role != null || mem.communicationTone != null
                        if (hasProfile || mem.topTopics?.isNotEmpty() == true || mem.documentPreferences?.isNotEmpty() == true || mem.lastQuestion != null) {

                            HorizontalDivider()

                            // 基础画像
                            Text(
                                text = "基础画像",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    ProfileRow(label = "部门", value = mem.department ?: "未填写")
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    )
                                    ProfileRow(label = "岗位", value = mem.role ?: "未填写")
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    )
                                    ProfileRow(label = "沟通语气", value = communicationTone)
                                }
                            }

                            // 常问主题
                            mem.topTopics?.takeIf { it.isNotEmpty() }?.let { topics ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "常问主题",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    topics.take(6).forEach { topic ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = topic.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "${topic.count} 次",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }
                            }

                            // 常用资料
                            mem.documentPreferences?.takeIf { it.isNotEmpty() }?.let { docPrefs ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "常用资料",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    docPrefs.take(4).forEach { doc ->
                                        Text(
                                            text = "• ${doc.name}  (${doc.count} 次)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // 最近一次问题
                            mem.lastQuestion?.let { lastQ ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "最近一次问题",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = lastQ,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Clear memory (matches web bottom action) ──
                    OutlinedButton(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = (uiState.memory?.questionCount ?: 0) > 0,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Filled.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("清空 AI 记忆")
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun ProfileRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
        )
    }
}
