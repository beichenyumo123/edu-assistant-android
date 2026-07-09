package com.zxxf.assistant.ui.chat.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.annotator.annotatorSettings
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownTable
import com.mikepenz.markdown.compose.elements.MarkdownTableBasicText
import com.zxxf.assistant.ui.theme.Surface0
import com.zxxf.assistant.ui.theme.Surface1
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMTokenTypes

private const val TAG = "MarkdownTable"

// ── Public API ─────────────────────────────────────────────────────────────

val catppuccinMarkdownComponents: MarkdownComponents = run {
    Log.i(TAG, "Initializing catppuccinMarkdownComponents")
    markdownComponents(
        table = { model ->
            Log.d(TAG, "table: ${model.node.children.size} children")
            MarkdownTable(
                content = model.content,
                node = model.node,
                style = model.typography.table,
                headerBlock = { content, headerNode, tableWidth, style ->
                    CatppuccinTableHeader(content, headerNode, tableWidth, style)
                },
                rowBlock = { content, rowNode, tableWidth, style ->
                    CatppuccinTableRow(content, rowNode, tableWidth, style)
                },
            )
        }
    ).also {
        Log.i(TAG, "Initialized OK")
    }
}

// ── Shared constants ───────────────────────────────────────────────────────

private val CELL_BORDER = 0.5.dp
private val CELL_PAD_H = 12.dp
private val CELL_PAD_V = 8.dp

// ── Header row ─────────────────────────────────────────────────────────────

@Composable
private fun CatppuccinTableHeader(
    content: String,
    headerNode: ASTNode,
    tableWidth: Dp,
    style: androidx.compose.ui.text.TextStyle,
) {
    val annotatorSettings = annotatorSettings()
    val cells = remember(headerNode) {
        headerNode.children.filter { it.type == GFMTokenTypes.CELL }
    }

    Row(
        modifier = Modifier
            .widthIn(tableWidth)
            .height(IntrinsicSize.Max)       // 核心修复 1：整行高度 = 最高单元格
            .background(Surface0),
    ) {
        cells.forEach { cell ->
            Box(
                modifier = Modifier
                    .weight(1f)              // 核心修复 2：上下行同列对齐
                    .fillMaxHeight()         // 核心修复 3：矮单元格撑满行高
                    .border(CELL_BORDER, Surface1)
                    .padding(horizontal = CELL_PAD_H, vertical = CELL_PAD_V),
                contentAlignment = Alignment.CenterStart,
            ) {
                MarkdownTableBasicText(
                    content = content,
                    cell = cell,
                    style = style.copy(fontWeight = FontWeight.Bold),
                    maxLines = Int.MAX_VALUE,
                    overflow = TextOverflow.Clip,
                    annotatorSettings = annotatorSettings,
                )
            }
        }
    }
}

// ── Data row ───────────────────────────────────────────────────────────────

@Composable
private fun CatppuccinTableRow(
    content: String,
    rowNode: ASTNode,
    tableWidth: Dp,
    style: androidx.compose.ui.text.TextStyle,
) {
    val annotatorSettings = annotatorSettings()
    val cells = remember(rowNode) {
        rowNode.children.filter { it.type == GFMTokenTypes.CELL }
    }

    Row(
        modifier = Modifier
            .widthIn(tableWidth)
            .height(IntrinsicSize.Max),      // 核心修复 1
    ) {
        cells.forEach { cell ->
            Box(
                modifier = Modifier
                    .weight(1f)              // 核心修复 2
                    .fillMaxHeight()         // 核心修复 3
                    .border(CELL_BORDER, Surface1)
                    .padding(horizontal = CELL_PAD_H, vertical = CELL_PAD_V),
                contentAlignment = Alignment.CenterStart,
            ) {
                MarkdownTableBasicText(
                    content = content,
                    cell = cell,
                    style = style,
                    maxLines = Int.MAX_VALUE,
                    overflow = TextOverflow.Clip,
                    annotatorSettings = annotatorSettings,
                )
            }
        }
    }
}
