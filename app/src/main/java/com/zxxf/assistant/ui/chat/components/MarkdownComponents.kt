package com.zxxf.assistant.ui.chat.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.compose.elements.MarkdownHeader
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes

private const val TAG = "MarkdownTable"

// ── Public API ─────────────────────────────────────────────────────────────

val catppuccinMarkdownComponents: MarkdownComponents = run {
    Log.i(TAG, "Initializing catppuccinMarkdownComponents")
    markdownComponents(
        table = { model ->
            Log.d(TAG, "table: ${model.node.children.size} children")
            CatppuccinMarkdownTable(
                content = model.content,
                node = model.node,
            )
        },
        // ── Heading overrides — 显式注入 MaterialTheme.typography ────────
        // 调用 MarkdownHeader（库默认渲染器）而非 Text()，
        // 保留 buildMarkdownAnnotatedString 递归渲染内联格式（加粗/斜体/代码/链接）
        heading1 = { model ->
            MarkdownHeader(
                content = model.content,
                node = model.node,
                style = MaterialTheme.typography.headlineLarge,
            )
        },
        heading2 = { model ->
            MarkdownHeader(
                content = model.content,
                node = model.node,
                style = MaterialTheme.typography.headlineMedium,
            )
        },
        heading3 = { model ->
            MarkdownHeader(
                content = model.content,
                node = model.node,
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        heading4 = { model ->
            MarkdownHeader(
                content = model.content,
                node = model.node,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        heading5 = { model ->
            MarkdownHeader(
                content = model.content,
                node = model.node,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        heading6 = { model ->
            MarkdownHeader(
                content = model.content,
                node = model.node,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    ).also { Log.i(TAG, "Initialized OK") }
}

// ── Constants ──────────────────────────────────────────────────────────────

private fun columnWidth(colIndex: Int) = when (colIndex) {
    0 -> 168.dp
    1 -> 260.dp
    else -> 160.dp
}
private val CELL_BORDER = 1.dp
private val TABLE_BORDER = 1.dp
private val CELL_PAD_H = 10.dp
private val CELL_PAD_V = 9.dp
private val TABLE_RADIUS = 16.dp

// ── Table container ────────────────────────────────────────────────────────

@Composable
private fun CatppuccinMarkdownTable(
    content: String,
    node: ASTNode,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    val rows = remember(content, node) {
        var dataIdx = 0
        node.children.mapNotNull { child ->
            when (child.type) {
                GFMElementTypes.HEADER -> TableRowData(
                    cells = child.tableCells(content),
                    isHeader = true,
                    dataIdx = -1,
                )
                GFMElementTypes.ROW -> TableRowData(
                    cells = child.tableCells(content),
                    isHeader = false,
                    dataIdx = dataIdx++,
                )
                else -> null
            }
        }.also { Log.d(TAG, "Parsed ${it.size} rows") }
    }
    val header = rows.firstOrNull { it.isHeader }
    val dataRows = rows.filterNot { it.isHeader }
    val columnCount = rows.firstOrNull()?.cells?.size ?: 0

    if (columnCount == 2 && dataRows.isNotEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            dataRows.forEach { row ->
                ResponsiveTwoColumnRow(row = row, header = header)
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .horizontalScroll(scrollState),
        ) {
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .border(
                        TABLE_BORDER,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                        RoundedCornerShape(TABLE_RADIUS),
                    ),
            ) {
                rows.forEach { row ->
                    TableGridRow(row)
                }
            }
        }
    }
}

private data class TableRowData(
    val cells: List<String>,
    val isHeader: Boolean,
    val dataIdx: Int,
)

private fun ASTNode.tableCells(content: String): List<String> {
    return children.filter { it.type == GFMTokenTypes.CELL }.map { cellNode ->
        try {
            content.substring(cellNode.startOffset, cellNode.endOffset).trim()
        } catch (_: Exception) {
            ""
        }.replace("<br>", "\n")
            .replace("<br/>", "\n")
            .replace("<br />", "\n")
    }
}

// ── Mobile-first two-column table ──────────────────────────────────────────

@Composable
private fun ResponsiveTwoColumnRow(
    row: TableRowData,
    header: TableRowData?,
) {
    val firstLabel = header?.cells?.getOrNull(0).toTableLabel("项目")
    val secondLabel = header?.cells?.getOrNull(1).toTableLabel("说明")

    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            TableFieldLabel(firstLabel)
            Spacer(modifier = Modifier.height(5.dp))
            TableMarkdownText(
                text = row.cells.getOrElse(0) { "" },
                isHeader = true,
                compact = false,
            )

            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
            )

            TableFieldLabel(secondLabel)
            Spacer(modifier = Modifier.height(5.dp))
            TableMarkdownText(
                text = row.cells.getOrElse(1) { "" },
                isHeader = false,
                compact = false,
            )
        }
    }
}

@Composable
private fun TableFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
    )
}

private fun String?.toTableLabel(fallback: String): String {
    return this
        ?.replace("**", "")
        ?.replace("__", "")
        ?.replace("\n", " ")
        ?.trim()
        ?.ifBlank { fallback }
        ?: fallback
}

// ── Scrollable grid table for 3+ columns ───────────────────────────────────

@Composable
private fun TableGridRow(row: TableRowData) {
    val bgColor = when {
        row.isHeader -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f)
        row.dataIdx % 2 == 1 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        else -> Color.Transparent
    }
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Max)
            .background(bgColor),
    ) {
        row.cells.forEachIndexed { colIndex, cellText ->
            TableGridCell(
                text = cellText,
                isHeader = row.isHeader,
                colIndex = colIndex,
            )
        }
    }
}

@Composable
private fun TableGridCell(
    text: String,
    isHeader: Boolean,
    colIndex: Int,
) {
    Box(
        modifier = Modifier
            .width(columnWidth(colIndex))
            .fillMaxHeight()
            .border(CELL_BORDER, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
            .padding(horizontal = CELL_PAD_H, vertical = CELL_PAD_V),
        contentAlignment = Alignment.TopStart,
    ) {
        TableMarkdownText(
            text = text,
            isHeader = isHeader,
            compact = true,
        )
    }
}

// ── Single cell markdown ───────────────────────────────────────────────────

@Composable
private fun TableMarkdownText(
    text: String,
    isHeader: Boolean,
    compact: Boolean,
) {
    val baseStyle = if (compact) {
        MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp)
    } else {
        MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp)
    }
    val cellTextStyle = baseStyle.copy(
        fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Normal,
    )

    CompositionLocalProvider(LocalTextStyle provides cellTextStyle) {
        Markdown(
            content = text.ifEmpty { "—" },
            colors = markdownColor(
                codeBackground = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
