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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.zxxf.assistant.data.dto.EvaluationDto
import com.zxxf.assistant.ui.theme.Green500
import com.zxxf.assistant.ui.theme.Orange500
import com.zxxf.assistant.ui.theme.Red500

/**
 * Returns a color based on score value.
 * - >= 0.7 → green
 * - 0.4–0.7 → orange
 * - < 0.4 → red
 * For hallucination_risk, the scale is reversed (lower is better).
 */
@Composable
private fun scoreColor(score: Double?, reversed: Boolean = false): Color {
    if (score == null) return MaterialTheme.colorScheme.outline
    val v = if (reversed) 1.0 - score else score
    return when {
        v >= 0.7 -> Green500
        v >= 0.4 -> Orange500
        else -> Red500
    }
}

private fun scorePercent(score: Double?): String {
    if (score == null) return "—"
    return "${"%.0f".format(score * 100)}%"
}

private fun riskLevelLabel(level: String?): String = when (level?.lowercase()) {
    "low", "低" -> "低风险"
    "medium", "中" -> "中风险"
    "high", "高" -> "高风险"
    else -> level ?: "—"
}

@Composable
private fun riskLevelColor(level: String?): Color = when (level?.lowercase()) {
    "low", "低" -> Green500
    "medium", "中" -> Orange500
    "high", "高" -> Red500
    else -> MaterialTheme.colorScheme.outline
}

/**
 * Collapsible panel showing RAG evaluation metrics for an assistant response.
 */
@Composable
fun EvaluationPanel(
    evaluation: EvaluationDto?,
    modifier: Modifier = Modifier
) {
    if (evaluation == null) return

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
                Icons.Filled.Assessment,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "RAG 评价指标",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            // Overall score badge
            evaluation.overallScore?.let { score ->
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = scoreColor(score).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "总分 ${"%.0f".format(score * 100)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = scoreColor(score),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
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
                // Risk level banner
                evaluation.riskLevel?.let { level ->
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = riskLevelColor(level).copy(alpha = 0.1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = riskLevelColor(level)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "风险等级：${riskLevelLabel(level)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = riskLevelColor(level),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Core metrics grid (2 columns)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        label = "检索质量",
                        score = evaluation.retrievalQuality,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "证据支撑",
                        score = evaluation.groundedness,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        label = "引用覆盖",
                        score = evaluation.citationCoverage,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "引用正确",
                        score = evaluation.citationValidity,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        label = "幻觉风险",
                        score = evaluation.hallucinationRisk,
                        reversed = true,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "上下文重叠",
                        score = evaluation.generation?.contextOverlap,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Retrieval details
                evaluation.retrieval?.let { retrieval ->
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "检索详情",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailChip("Top-K", "${retrieval.topK ?: "—"}", Modifier.weight(1f))
                        DetailChip("命中", if (retrieval.retrievalHit == true) "是" else "否", Modifier.weight(1f))
                        DetailChip("片段数", "${retrieval.retrievedChunks ?: "—"}", Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailChip("最佳相关", scorePercent(retrieval.bestRelevance), Modifier.weight(1f))
                        DetailChip("平均相关", scorePercent(retrieval.meanRelevance), Modifier.weight(1f))
                        DetailChip("文档多样性", scorePercent(retrieval.documentDiversity), Modifier.weight(1f))
                    }
                }

                // Generation details
                evaluation.generation?.let { gen ->
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "生成详情",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailChip("声明数", "${gen.claimCount ?: "—"}", Modifier.weight(1f))
                        DetailChip("有支撑", "${gen.supportedClaimCount ?: "—"}", Modifier.weight(1f))
                        DetailChip("无支撑", "${gen.unsupportedClaimCount ?: "—"}", Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailChip("有效引用", "${gen.validCitationCount ?: "—"}", Modifier.weight(1f))
                        DetailChip("无效引用", "${gen.invalidCitationCount ?: "—"}", Modifier.weight(1f))
                        DetailChip("来源利用", scorePercent(gen.sourceUtilization), Modifier.weight(1f))
                    }
                }

                // Notes
                evaluation.notes?.let { notes ->
                    if (notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "备注",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        notes.forEach { note ->
                            Text(
                                text = "• $note",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    score: Double?,
    reversed: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = scorePercent(score),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = scoreColor(score, reversed)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DetailChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
