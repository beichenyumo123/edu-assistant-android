package com.zxxf.assistant.ui.chat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * A horizontal bar shown above the input area that indicates the current
 * document scope selection status for retrieval filtering.
 *
 * States:
 * - No documents uploaded: hidden (returns nothing)
 * - All documents selected (or none explicitly selected): "全部资料"
 * - Some documents selected: "已选 N 份"
 */
@Composable
fun DocumentScopeBar(
    totalDocumentCount: Int,
    selectedDocCount: Int,
    onOpenKnowledgeSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Don't show if no documents exist
    if (totalDocumentCount == 0) return

    val (label, icon) = when {
        selectedDocCount == 0 -> "全部资料" to Icons.Filled.Public
        selectedDocCount == totalDocumentCount -> "全部资料 ($totalDocumentCount 份)" to Icons.Filled.Public
        else -> "已选 $selectedDocCount / $totalDocumentCount 份" to Icons.Filled.FilterList
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenKnowledgeSheet() }
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "选择文档",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
