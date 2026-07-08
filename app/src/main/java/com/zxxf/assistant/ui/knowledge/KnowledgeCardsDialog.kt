package com.zxxf.assistant.ui.knowledge

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import com.zxxf.assistant.data.dto.KnowledgePointDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeCardsDialog(
    isExtracting: Boolean,
    knowledgePoints: List<KnowledgePointDto>?,
    documentName: String,
    onDismiss: () -> Unit,
    onAskQuestion: (String) -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "知识卡片",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                knowledgePoints?.let {
                    Text(
                        text = "${it.size} 项",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
            ) {
                Text(
                    text = documentName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (isExtracting) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "正在提取知识点...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else if (knowledgePoints != null) {
                    // Group by category
                    val grouped = knowledgePoints.groupBy { it.category ?: "未分类" }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        grouped.forEach { (category, points) ->
                            item(key = "header_$category") {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                                HorizontalDivider()
                            }
                            items(points, key = { "kp_${it.title}_${points.indexOf(it)}" }) { point ->
                                KnowledgeCardItem(
                                    point = point,
                                    onAskQuestion = {
                                        onAskQuestion("请围绕'${point.title}'展开讲解...")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // Export as Markdown
                val md = buildMarkdownExport(knowledgePoints, documentName)
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("knowledge_cards", md))
                Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("导出 Markdown")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun KnowledgeCardItem(
    point: KnowledgePointDto,
    onAskQuestion: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onAskQuestion,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = point.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "追问",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = point.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Key points
            point.keyPoints?.takeIf { it.isNotEmpty() }?.let { kps ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "要点",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                kps.forEach { kp ->
                    Text(
                        text = "• $kp",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Examples
            point.examples?.takeIf { it.isNotEmpty() }?.let { examples ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "示例",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.secondary
                )
                examples.take(3).forEach { ex ->
                    Text(
                        text = "\"$ex\"",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Source excerpt
            point.sourceExcerpt?.let { excerpt ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "来源: $excerpt",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 2
                )
            }
        }
    }
}

private fun buildMarkdownExport(points: List<KnowledgePointDto>?, documentName: String): String {
    if (points.isNullOrEmpty()) return "# 知识卡片 - $documentName\n\n暂无内容"

    val grouped = points.groupBy { it.category ?: "未分类" }
    val sb = StringBuilder()
    sb.appendLine("# 知识卡片 - $documentName")
    sb.appendLine()

    grouped.forEach { (category, catPoints) ->
        sb.appendLine("## $category")
        sb.appendLine()
        catPoints.forEach { point ->
            sb.appendLine("### ${point.title}")
            sb.appendLine()
            sb.appendLine(point.description)
            sb.appendLine()
            point.keyPoints?.forEach { kp ->
                sb.appendLine("- $kp")
            }
            sb.appendLine()
            point.examples?.take(3)?.forEach { ex ->
                sb.appendLine("> $ex")
                sb.appendLine()
            }
            point.sourceExcerpt?.let { excerpt ->
                sb.appendLine("*来源: $excerpt*")
                sb.appendLine()
            }
            sb.appendLine("---")
            sb.appendLine()
        }
    }
    return sb.toString()
}
