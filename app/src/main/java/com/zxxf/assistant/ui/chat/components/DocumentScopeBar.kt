package com.zxxf.assistant.ui.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
 * A pill-shaped tag shown inside the unified input container that indicates
 * the current document scope selection for retrieval filtering.
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
    if (totalDocumentCount == 0) return

    val (label, icon) = when {
        selectedDocCount == 0 -> "全部资料" to Icons.Filled.Public
        selectedDocCount == totalDocumentCount -> "全部资料 ($totalDocumentCount 份)" to Icons.Filled.Public
        else -> "已选 $selectedDocCount / $totalDocumentCount 份" to Icons.Filled.FilterList
    }

    Surface(
        modifier = modifier.clickable { onOpenKnowledgeSheet() },
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
