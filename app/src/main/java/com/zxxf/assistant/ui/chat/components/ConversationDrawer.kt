package com.zxxf.assistant.ui.chat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zxxf.assistant.data.dto.ConversationDto

@Composable
fun ConversationDrawerContent(
    conversations: List<ConversationDto>,
    currentConversationId: Long?,
    isLoading: Boolean = false,
    onNewConversation: () -> Unit,
    onSelectConversation: (Long) -> Unit,
    onDeleteConversation: (Long) -> Unit,
    onRefresh: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var deleteConfirmId by remember { mutableStateOf<Long?>(null) }

    Column(modifier = modifier.fillMaxHeight()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "对话历史",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Row {
                onRefresh?.let { refreshAction ->
                    IconButton(onClick = refreshAction) {
                        Icon(Icons.Filled.Refresh, contentDescription = "刷新")
                    }
                }
                IconButton(onClick = onNewConversation) {
                    Icon(Icons.Filled.Add, contentDescription = "新对话")
                }
            }
        }

        HorizontalDivider()

        // Conversation list
        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无对话",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(conversations, key = { it.id }) { conv ->
                    val isSelected = conv.id == currentConversationId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectConversation(conv.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = conv.title,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = { deleteConfirmId = conv.id },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "删除",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        // Delete confirmation dialog
        if (deleteConfirmId != null) {
            AlertDialog(
                onDismissRequest = { deleteConfirmId = null },
                title = { Text("删除对话") },
                text = { Text("确定删除此对话？") },
                confirmButton = {
                    TextButton(onClick = {
                        deleteConfirmId?.let { onDeleteConversation(it) }
                        deleteConfirmId = null
                    }) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteConfirmId = null }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}
