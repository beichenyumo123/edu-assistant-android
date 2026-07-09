package com.zxxf.assistant.ui.chat.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import com.zxxf.assistant.ui.theme.Base
import com.zxxf.assistant.ui.theme.Surface0
import com.zxxf.assistant.ui.theme.Surface1
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
    0 -> 80.dp
    1 -> 340.dp
    else -> 160.dp
}
private val CELL_BORDER = 0.5.dp
private val TABLE_BORDER = 0.5.dp
private val CELL_PAD_H = 8.dp
private val CELL_PAD_V = 8.dp
private val TABLE_RADIUS = 4.dp

// ── Table container ────────────────────────────────────────────────────────

@Composable
private fun CatppuccinMarkdownTable(
    content: String,
    node: ASTNode,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    val rows = remember(node) {
        var dataIdx = 0
        node.children.mapNotNull { child ->
            when (child.type) {
                GFMElementTypes.HEADER -> RowInfo(child, isHeader = true, dataIdx = -1)
                GFMElementTypes.ROW -> RowInfo(child, isHeader = false, dataIdx = dataIdx++)
                else -> null
            }
        }.also { Log.d(TAG, "Parsed ${it.size} rows") }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
    ) {
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .border(TABLE_BORDER, Surface1, RoundedCornerShape(TABLE_RADIUS)),
        ) {
            rows.forEach { rowInfo ->
                if (rowInfo.isHeader) {
                    TableHeaderRow(rowInfo.node, content)
                } else {
                    TableDataRow(rowInfo.node, rowInfo.dataIdx, content)
                }
            }
        }
    }
}

private data class RowInfo(val node: ASTNode, val isHeader: Boolean, val dataIdx: Int)

// ── Header row ─────────────────────────────────────────────────────────────

@Composable
private fun TableHeaderRow(rowNode: ASTNode, content: String) {
    val cells = remember(rowNode) {
        rowNode.children.filter { it.type == GFMTokenTypes.CELL }
    }
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Max)
            .background(Surface0),
    ) {
        cells.forEachIndexed { colIndex, cellNode ->
            TableCell(cellNode, isHeader = true, colIndex = colIndex, content = content)
        }
    }
}

// ── Data row ───────────────────────────────────────────────────────────────

@Composable
private fun TableDataRow(rowNode: ASTNode, rowIndex: Int, content: String) {
    val cells = remember(rowNode) {
        rowNode.children.filter { it.type == GFMTokenTypes.CELL }
    }
    val bgColor = if (rowIndex % 2 == 0) Color.Transparent
        else Base.copy(alpha = 0.4f)

    Row(
        modifier = Modifier
            .height(IntrinsicSize.Max)
            .background(bgColor),
    ) {
        cells.forEachIndexed { colIndex, cellNode ->
            TableCell(cellNode, isHeader = false, colIndex = colIndex, content = content)
        }
    }
}

// ── Single cell (nested-Markdown approach) ─────────────────────────────────

/**
 * Two-pass parser for table cells:
 * 1. Outer pass — raw content keeps `<br>` intact so the GFM parser sees a
 *    valid single-line table row → the table AST stays solid.
 * 2. Inner pass — extract this cell's raw markdown from the source offsets,
 *    replace `<br>` → `\n` locally, then render with a **nested** [Markdown]
 *    component.  This inner component handles `**bold**`, `- lists`,
 *    and multi-line content correctly.
 *
 * The nested [Markdown] deliberately omits custom `components` so that any
 * nested table inside a cell uses library defaults — no infinite recursion.
 */
@Composable
private fun TableCell(
    cellNode: ASTNode,
    isHeader: Boolean,
    colIndex: Int,
    content: String,
) {
    // Extract this cell's raw markdown source — no global <br> substitution
    val rawCellText = remember(cellNode) {
        try {
            content.substring(cellNode.startOffset, cellNode.endOffset).trim()
        } catch (_: Exception) {
            "" // should never happen; offsets are always valid
        }
    }

    // Local <br> → \n inside this cell only — outer table AST is unaffected
    val processed = remember(rawCellText) {
        rawCellText
            .replace("<br>", "\n")
            .replace("<br/>", "\n")
            .replace("<br />", "\n")
    }

    val cellTextStyle = MaterialTheme.typography.bodySmall.copy(
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        lineHeight = 18.sp,
    )

    Box(
        modifier = Modifier
            .width(columnWidth(colIndex))
            .fillMaxHeight()
            .border(CELL_BORDER, Surface1)
            .padding(horizontal = CELL_PAD_H, vertical = CELL_PAD_V),
        contentAlignment = Alignment.TopStart,
    ) {
        CompositionLocalProvider(LocalTextStyle provides cellTextStyle) {
            // Nested Markdown — renders **bold**, lists, multi-line inside cell
            // No custom components → library defaults, safe from recursion
            Markdown(
                content = processed.ifEmpty { rawCellText },
                colors = markdownColor(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
