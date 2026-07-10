package com.zxxf.assistant.ui.chat.memory

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zxxf.assistant.data.repository.MemoryRepository
import com.zxxf.assistant.ui.theme.Base
import com.zxxf.assistant.ui.theme.Surface0
import com.zxxf.assistant.ui.theme.Surface1
import com.zxxf.assistant.ui.theme.Text

private val answerStyleOptions = listOf("结构化", "简洁", "详细", "步骤化", "表格化")
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
    val memory = uiState.memory
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 本地编辑状态 (从 API 数据初始化)
    var localMemoryEnabled by remember(memory) { mutableStateOf(memory?.memoryEnabled ?: true) }
    var localStyle by remember(memory) { mutableStateOf(memory?.preferredAnswerStyle) }
    var localTone by remember(memory) { mutableStateOf(memory?.communicationTone) }

    // 未保存更改检测
    val hasChanges = localMemoryEnabled != memory?.memoryEnabled ||
            localStyle != memory?.preferredAnswerStyle ||
            localTone != memory?.communicationTone

    // Reload memory data on open / refreshKey bump
    LaunchedEffect(Unit) { viewModel.loadMemory() }
    LaunchedEffect(refreshKey) { if (refreshKey > 0) viewModel.loadMemory() }

    // Toast 反馈
    LaunchedEffect(uiState.saveSuccess, uiState.clearSuccess, uiState.error) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "设置已保存", Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
        if (uiState.clearSuccess) {
            Toast.makeText(context, "记忆已清空", Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        // ── 悬浮大圆角容器 ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Base)
        ) {
            // ── 拖拽条 ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Surface1)
                )
            }

            // ── 关闭按钮 (右上角) ─────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "关闭",
                        tint = Text.copy(alpha = 0.5f)
                    )
                }
            }

            // ── 可滚动内容 ───────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 20.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ── 标题区域 ─────────────────────────────────────────────
                item(key = "header") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Memory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "智能记忆",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Text
                        )
                    }
                    Text(
                        text = "AI 会在对话中学习您的偏好，提供更个性化的回答",
                        style = MaterialTheme.typography.bodySmall,
                        color = Text.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // ── 偏好设置卡片 ─────────────────────────────────────────
                item(key = "settings_card") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Surface0)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 开关
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "启用长期记忆",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = Text
                            )
                            Switch(
                                checked = localMemoryEnabled,
                                onCheckedChange = { localMemoryEnabled = it }
                            )
                        }

                        HorizontalDivider(color = Surface1, thickness = 0.5.dp)

                        // 下拉菜单组
                        StyledDropdown(
                            label = "回答风格",
                            value = localStyle,
                            options = answerStyleOptions,
                            onValueChange = { localStyle = it }
                        )

                        StyledDropdown(
                            label = "沟通语气",
                            value = localTone,
                            options = communicationToneOptions,
                            onValueChange = { localTone = it }
                        )

                        // 保存按钮 (胶囊)
                        Button(
                            onClick = {
                                viewModel.saveSettings(
                                    memoryEnabled = localMemoryEnabled,
                                    preferredAnswerStyle = localStyle,
                                    communicationTone = localTone
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = hasChanges && !uiState.isSaving,
                            shape = RoundedCornerShape(50)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("保存设置", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ── 记忆数据统计 ─────────────────────────────────────────
                item(key = "stats") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(0.5.dp, Surface1, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            icon = Icons.Filled.ChatBubbleOutline,
                            label = "记录问题",
                            value = "${memory?.questionCount ?: 0} 条"
                        )
                        StatItem(
                            icon = Icons.Filled.Update,
                            label = "最近更新",
                            value = memory?.updatedAt?.take(10) ?: "暂无"
                        )
                    }
                }

                // ── 基础画像 ─────────────────────────────────────────────
                val dept = memory?.department
                val role = memory?.role
                if (dept != null || role != null) {
                    item(key = "profile_header") {
                        Text(
                            text = "基础画像",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Text
                        )
                    }

                    item(key = "profile_card") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Surface0)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ProfileRow(label = "部门", value = dept ?: "未填写")
                            if (role != null) {
                                HorizontalDivider(color = Surface1, thickness = 0.5.dp)
                                ProfileRow(label = "岗位", value = role)
                            }
                            memory?.communicationTone?.let { tone ->
                                HorizontalDivider(color = Surface1, thickness = 0.5.dp)
                                ProfileRow(label = "沟通语气", value = tone)
                            }
                        }
                    }
                }

                // ── 常问主题 ─────────────────────────────────────────────
                memory?.topTopics?.takeIf { it.isNotEmpty() }?.let { topics ->
                    item(key = "topics_header") {
                        Text(
                            text = "常问主题",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Text
                        )
                    }
                    items(topics.take(6), key = { "topic_${it.name}" }) { topic ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Surface0)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = topic.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Text,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${topic.count} 次",
                                style = MaterialTheme.typography.labelSmall,
                                color = Text.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // ── 常用资料 ─────────────────────────────────────────────
                memory?.documentPreferences?.takeIf { it.isNotEmpty() }?.let { docs ->
                    item(key = "docs_header") {
                        Text(
                            text = "常用资料",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Text
                        )
                    }
                    items(docs.take(4), key = { "doc_${it.name}" }) { doc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Surface0)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = doc.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Text,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${doc.count} 次",
                                style = MaterialTheme.typography.labelSmall,
                                color = Text.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // ── 最近一次问题 ─────────────────────────────────────────
                memory?.lastQuestion?.let { lastQ ->
                    item(key = "lastq_header") {
                        Text(
                            text = "最近一次问题",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Text
                        )
                    }
                    item(key = "lastq_card") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Surface0)
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Filled.QuestionAnswer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = lastQ,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Text
                            )
                        }
                    }
                }

                // ── 清空按钮 ─────────────────────────────────────────────
                item(key = "clear") {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { viewModel.clearMemory() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        ),
                        enabled = !uiState.isClearing && (memory?.questionCount ?: 0) > 0
                    ) {
                        if (uiState.isClearing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.error,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("清空所有记忆", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ── 现代化下拉框封装 ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StyledDropdown(
    label: String,
    value: String?,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Text.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = value ?: "系统默认",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Base,
                    unfocusedContainerColor = Base,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Surface1,
                    focusedTextColor = Text,
                    unfocusedTextColor = Text
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Base)
            ) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption, color = Text) },
                        onClick = {
                            onValueChange(selectionOption)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// ── 统计项 ──────────────────────────────────────────────────────────────────

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Text
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Text.copy(alpha = 0.6f)
        )
    }
}

// ── 画像行 ──────────────────────────────────────────────────────────────────

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
            style = MaterialTheme.typography.labelMedium,
            color = Text.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Text
        )
    }
}
