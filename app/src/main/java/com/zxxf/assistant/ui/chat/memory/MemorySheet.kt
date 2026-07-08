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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zxxf.assistant.data.repository.MemoryRepository

private val answerStyleOptions = listOf("详细全面", "简洁精炼", "分点概括", "引导式")
private val communicationToneOptions = listOf("正式专业", "轻松友好", "鼓励激励", "严谨学术")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemorySheet(
    memoryRepository: MemoryRepository,
    onDismiss: () -> Unit
) {
    val viewModel: MemoryViewModel = viewModel(
        factory = MemoryViewModel.Factory(memoryRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    // Local edit state (copied from API data on first load)
    var memoryEnabled by remember { mutableStateOf(true) }
    var answerStyle by remember { mutableStateOf("详细全面") }
    var communicationTone by remember { mutableStateOf("正式专业") }
    var answerStyleExpanded by remember { mutableStateOf(false) }
    var toneExpanded by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    // Populate from API response on first load
    LaunchedEffect(uiState.memory) {
        if (!initialized && uiState.memory != null) {
            uiState.memory!!.let { mem ->
                memoryEnabled = mem.memoryEnabled
                mem.preferredAnswerStyle?.let { answerStyle = it }
                mem.communicationTone?.let { communicationTone = it }
            }
            initialized = true
        }
    }

    LaunchedEffect(uiState.clearSuccess) {
        if (uiState.clearSuccess) {
            onDismiss()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    // Clear confirmation
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
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AI 记忆",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "个性化使用画像",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "关闭")
                    }
                }
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
                    // ── Stats area ──
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                icon = Icons.Filled.QuestionAnswer,
                                label = "问题数",
                                value = "${uiState.memory?.questionCount ?: 0}"
                            )
                            StatItem(
                                icon = if (memoryEnabled) Icons.Filled.Memory else Icons.Filled.Memory,
                                label = "记忆状态",
                                value = if (memoryEnabled) "已启用" else "已禁用",
                                valueColor = if (memoryEnabled) MaterialTheme.colorScheme.tertiary
                                    else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    HorizontalDivider()

                    // ── Settings area ──
                    Text(
                        text = "设置",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    // Memory toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "启用 AI 记忆",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = "允许 AI 记录你的偏好以优化回答",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(checked = memoryEnabled, onCheckedChange = { memoryEnabled = it })
                    }

                    // Answer style
                    ExposedDropdownMenuBox(
                        expanded = answerStyleExpanded,
                        onExpandedChange = { answerStyleExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = answerStyle,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("回答风格") },
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

                    // Communication tone
                    ExposedDropdownMenuBox(
                        expanded = toneExpanded,
                        onExpandedChange = { toneExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = communicationTone,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("沟通语气") },
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

                    // ── Profile area ──
                    uiState.memory?.let { mem ->
                        if (mem.grade != null || mem.major != null || mem.topTopics?.isNotEmpty() == true || mem.lastQuestion != null) {
                            HorizontalDivider()
                            Text(
                                text = "画像",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )

                            // Department & Position
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                mem.grade?.let { dept ->
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("部门: $dept") },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Business, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    )
                                }
                                mem.major?.let { pos ->
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("岗位: $pos") },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    )
                                }
                            }

                            // Hot topics
                            mem.topTopics?.takeIf { it.isNotEmpty() }?.let { topics ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "热门话题",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    topics.entries.take(5).forEach { (topic, _) ->
                                        SuggestionChip(
                                            onClick = {},
                                            label = {
                                                Text(
                                                    text = topic,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            // Recent question
                            mem.lastQuestion?.let { lastQ ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "最近问题",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = lastQ,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }

                            // Document preferences
                            mem.documentPreferences?.takeIf { it.isNotEmpty() }?.let { docPrefs ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "常用文档",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                docPrefs.entries.take(3).forEach { (doc, count) ->
                                    Text(
                                        text = "• $doc ($count 次)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Action buttons ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.clearMemory()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("清空记忆")
                        }
                        Button(
                            onClick = {
                                viewModel.saveSettings(
                                    memoryEnabled = memoryEnabled,
                                    preferredAnswerStyle = answerStyle,
                                    communicationTone = communicationTone
                                )
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("保存设置")
                        }
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
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
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
