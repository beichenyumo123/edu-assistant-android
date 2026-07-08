package com.zxxf.assistant.ui.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zxxf.assistant.data.dto.AgentStepDto
import com.zxxf.assistant.ui.chat.AgentStepMsg

/**
 * Infers a Chinese tool/step category name from step text or tool_name,
 * matching the web frontend's inferStepTool() logic.
 */
fun inferStepTool(step: AgentStepMsg): String = inferStepTool(step.text, step.toolName)

fun inferStepTool(step: AgentStepDto): String = inferStepTool(
    text = step.text ?: step.step,
    toolName = step.toolName
)

private fun inferStepTool(text: String?, toolName: String?): String {
    val combined = "${text ?: ""}${toolName ?: ""}".lowercase()
    return when {
        combined.contains("检索") || combined.contains("文档片段")
            || combined.contains("知识库") || combined.contains("匹配")
            || combined.contains("retriev") || combined.contains("search")
            || combined.contains("vector") -> "企业知识检索"

        combined.contains("生成") || combined.contains("回答")
            || combined.contains("generat") || combined.contains("answer") -> "回答生成"

        combined.contains("分析") || combined.contains("analyz") -> "问题分析"

        combined.contains("摘要") || combined.contains("总结")
            || combined.contains("summar") -> "内容摘要"

        combined.contains("提取") || combined.contains("extract") -> "知识提取"

        text.isNullOrBlank() && toolName.isNullOrBlank() -> "处理中"
        else -> toolName ?: text ?: "处理中"
    }
}

fun inferStepIcon(step: AgentStepMsg): ImageVector = inferStepIconByTool(inferStepTool(step))

fun inferStepIcon(step: AgentStepDto): ImageVector = inferStepIconByTool(inferStepTool(step))

private fun inferStepIconByTool(tool: String): ImageVector = when {
    tool.contains("检索") -> Icons.Filled.Search
    tool.contains("生成") || tool.contains("回答") -> Icons.Filled.AutoAwesome
    tool.contains("分析") -> Icons.Filled.Psychology
    tool.contains("摘要") || tool.contains("总结") -> Icons.Filled.Summarize
    tool.contains("提取") -> Icons.Filled.ContentCopy
    else -> Icons.Filled.Settings
}

fun formatElapsed(ms: Long?): String {
    if (ms == null) return ""
    return when {
        ms < 1000 -> "${ms}ms"
        ms < 60_000 -> "${"%.1f".format(ms / 1000.0)}s"
        else -> "${ms / 60_000}m${(ms % 60_000) / 1000}s"
    }
}

/**
 * Collapsible panel showing agent thinking steps.
 *
 * @param steps The agent steps to display (from MessageUiItem.agentSteps after Done)
 * @param liveSteps Live thinking steps during streaming (from ChatUiState.thinkingSteps)
 */
@Composable
fun ThinkingSteps(
    steps: List<AgentStepDto>?,
    liveSteps: List<AgentStepMsg> = emptyList(),
    modifier: Modifier = Modifier
) {
    val displaySteps = when {
        !steps.isNullOrEmpty() -> steps.map { step ->
            AgentStepMsg(
                text = step.text ?: step.step ?: "",
                toolName = step.toolName,
                elapsedMs = step.elapsedMs
            )
        }
        liveSteps.isNotEmpty() -> liveSteps
        else -> return
    }

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        // Header — clickable to expand/collapse
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Psychology,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Agent 思考过程 (${displaySteps.size}步)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "收起" else "展开",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        }

        // Expandable steps list
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
            ) {
                displaySteps.forEachIndexed { index, step ->
                    ThinkingStepRow(step, index + 1)
                    if (index < displaySteps.lastIndex) {
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
private fun ThinkingStepRow(step: AgentStepMsg, number: Int) {
    val tool = inferStepTool(step)
    val icon = inferStepIconByTool(tool)
    val elapsed = formatElapsed(step.elapsedMs)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Step number badge
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

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tool,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            if (step.text.isNotBlank()) {
                Text(
                    text = step.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (elapsed.isNotEmpty()) {
            Text(
                text = elapsed,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}
