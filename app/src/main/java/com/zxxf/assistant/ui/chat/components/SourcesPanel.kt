package com.zxxf.assistant.ui.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.zxxf.assistant.data.dto.SourceDto

/**
 * Trust level color mapping.
 */
@Composable
private fun trustLevelColor(level: String?): androidx.compose.ui.graphics.Color = when (level?.lowercase()) {
    "high", "高" -> MaterialTheme.colorScheme.error.copy(alpha = 0f) // let chip use default
    "medium", "中" -> MaterialTheme.colorScheme.tertiary
    "low", "低" -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.outline
}

private fun trustLevelLabel(level: String?): String = when (level?.lowercase()) {
    "high" -> "高可信"
    "medium" -> "中可信"
    "low" -> "低可信"
    else -> level ?: "未知"
}

/**
 * Collapsible panel showing source citations for an assistant message.
 */
@Composable
fun SourcesPanel(
    sources: List<SourceDto>?,
    modifier: Modifier = Modifier
) {
    if (sources.isNullOrEmpty()) return

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Source,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "来源引用 (${sources.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            // Document count summary
            val docCount = sources.map { it.documentName }.filterNotNull().distinct().size
            Text(
                text = "${docCount}份文档",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "收起" else "展开",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
            ) {
                sources.forEachIndexed { index, source ->
                    SourceRow(source, index + 1)
                    if (index < sources.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceRow(source: SourceDto, number: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // First row: number + doc name + badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Source number badge
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$number",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Document name
            Text(
                text = source.documentName ?: source.evidenceId ?: "来源$number",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Trust level chip
            val tlLabel = trustLevelLabel(source.trustLevel)
            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        text = tlLabel,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                modifier = Modifier.height(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Second row: metadata
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Source type
            if (!source.sourceType.isNullOrBlank()) {
                Text(
                    text = source.sourceType,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Evidence ID
            if (!source.evidenceId.isNullOrBlank()) {
                if (!source.sourceType.isNullOrBlank()) {
                    Text(
                        text = " · ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Text(
                    text = source.evidenceId,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Retrieval score
            source.retrievalScore?.let { score ->
                Text(
                    text = "相似度 ${"%.3f".format(score)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // Text preview
        if (!source.text.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = source.text.take(150) + if (source.text.length > 150) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
