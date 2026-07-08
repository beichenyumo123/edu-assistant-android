package com.zxxf.assistant.ui.knowledge

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zxxf.assistant.data.dto.DocumentDto
import com.zxxf.assistant.data.repository.FileRepository
import com.zxxf.assistant.data.repository.ToolRepository
import com.zxxf.assistant.util.FileUtil

/**
 * ModalBottomSheet for knowledge base management.
 * Shows file list, upload button, delete via long-press.
 * Provides entry points to SummaryDialog and KnowledgeCardsDialog.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun KnowledgeSheet(
    fileRepository: FileRepository,
    toolRepository: ToolRepository,
    onDismiss: () -> Unit,
    onAskQuestion: ((String) -> Unit)? = null
) {
    val viewModel: KnowledgeViewModel = viewModel(
        factory = KnowledgeViewModel.Factory(fileRepository, toolRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // File picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            viewModel.uploadFile(context, uri)
        }
    }

    // Delete confirmation
    var deleteConfirmId by remember { mutableStateOf<Long?>(null) }
    var confirmDialogOpen by remember { mutableStateOf(false) }

    // Summary dialog
    var summaryTarget by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var knowledgeCardsTarget by remember { mutableStateOf<Pair<Long, String>?>(null) }

    // Ephemeral snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    // Summary Dialog
    summaryTarget?.let { (docId, docName) ->
        SummaryDialog(
            isSummarizing = uiState.isSummarizing,
            summary = uiState.summary,
            documentName = docName,
            onDismiss = {
                summaryTarget = null
                viewModel.clearSummary()
            },
            onCopy = { /* clipboard handled inside */ }
        )

        // Trigger summarize if not yet loaded
        LaunchedEffect(docId) {
            if (uiState.summary == null && !uiState.isSummarizing) {
                viewModel.summarize(docId, docName)
            }
        }
    }

    // Knowledge Cards Dialog
    knowledgeCardsTarget?.let { (docId, docName) ->
        KnowledgeCardsDialog(
            isExtracting = uiState.isExtracting,
            knowledgePoints = uiState.knowledgePoints,
            documentName = docName,
            onDismiss = {
                knowledgeCardsTarget = null
                viewModel.clearKnowledgeCards()
            },
            onAskQuestion = { question ->
                knowledgeCardsTarget = null
                viewModel.clearKnowledgeCards()
                onDismiss()
                onAskQuestion?.invoke(question)
            }
        )

        // Trigger extract if not yet loaded
        LaunchedEffect(docId) {
            if (uiState.knowledgePoints == null && !uiState.isExtracting) {
                viewModel.extractKnowledge(docId, docName)
            }
        }
    }

    // Delete confirmation dialog
    if (deleteConfirmId != null) {
        AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            title = { Text("删除文件") },
            text = { Text("确定删除此文件？相关向量数据也将被清除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteConfirmId?.let { viewModel.deleteFile(it) }
                        deleteConfirmId = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmId = null }) {
                    Text("取消")
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "知识库管理",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                FilledTonalButton(
                    onClick = {
                        filePickerLauncher.launch("*/*")
                    },
                    enabled = !uiState.isUploading
                ) {
                    if (uiState.isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("上传文件")
                }
            }

            HorizontalDivider()

            // File list or empty state
            if (uiState.documents.isEmpty()) {
                EmptyKnowledgeState(modifier = Modifier.fillMaxWidth().height(200.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.documents, key = { it.id }) { doc ->
                        KnowledgeFileItem(
                            document = doc,
                            onDelete = { deleteConfirmId = doc.id },
                            onSummarize = {
                                summaryTarget = Pair(doc.id, doc.originalName)
                            },
                            onExtractKnowledge = {
                                knowledgeCardsTarget = Pair(doc.id, doc.originalName)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KnowledgeFileItem(
    document: DocumentDto,
    onDelete: () -> Unit,
    onSummarize: () -> Unit,
    onExtractKnowledge: () -> Unit
) {
    var showActions by remember { mutableStateOf(false) }

    val statusLabel = when (document.status) {
        "ready" -> "已就绪"
        "processing" -> "处理中"
        "failed" -> "失败"
        else -> document.status
    }
    val statusColor = when (document.status) {
        "ready" -> MaterialTheme.colorScheme.tertiary
        "processing" -> MaterialTheme.colorScheme.secondary
        "failed" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { showActions = !showActions },
                onLongClick = onDelete
            )
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (showActions) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.Description,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = document.originalName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = statusColor.copy(alpha = 0.12f),
                        labelColor = statusColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = FileUtil.formatFileSize(document.fileSize),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                document.chunkCount?.let { count ->
                    Text(
                        text = "$count 个分块",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Error message for failed files
            document.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Action buttons when expanded
            AnimatedVisibility(visible = showActions) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = onSummarize,
                        enabled = document.status == "ready",
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.Summarize, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("制度速览", style = MaterialTheme.typography.labelMedium)
                    }
                    FilledTonalButton(
                        onClick = onExtractKnowledge,
                        enabled = document.status == "ready",
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("知识卡片", style = MaterialTheme.typography.labelMedium)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "删除",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyKnowledgeState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "知识库为空",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "上传 PDF、Word、TXT 或 Markdown 文件\nAI 将基于这些资料回答问题",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            lineHeight = MaterialTheme.typography.bodySmall.lineHeight
        )
    }
}
