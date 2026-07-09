package com.zxxf.assistant.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.annotator.AnnotatorSettings
import com.mikepenz.markdown.annotator.annotatorSettings
import com.mikepenz.markdown.annotator.buildMarkdownAnnotatedString
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.material.MarkdownBasicText
import com.zxxf.assistant.ui.theme.Surface0
import com.zxxf.assistant.ui.theme.Surface1
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes

// ── Cached table data ──────────────────────────────────────────────────────

private enum class RowType { HEADER, ROW, SEPARATOR }

private data class CachedCell(
    val annotatedText: AnnotatedString,
    val style: TextStyle,
)

private data class CachedRow(
    val type: RowType,
    val cells: List<CachedCell>,
)

// ── Public API ─────────────────────────────────────────────────────────────

/**
 * Catppuccin-themed Markdown component overrides.
 *
 * The custom table component:
 * - Pre-computes all cell [AnnotatedString]s in [remember] to avoid
 *   recomposition overhead during LazyColumn scroll
 * - Uses fillMaxWidth + weight(1f) cells (no nested horizontal scroll)
 * - Renders visible cell borders (0.5dp Surface1) and Surface0 header
 */
val catppuccinMarkdownComponents: MarkdownComponents = markdownComponents(
    table = { model ->
        CatppuccinMarkdownTable(
            content = model.content,
            node = model.node,
            style = model.typography.text,
        )
    }
)

/**
 * A custom Markdown table composable.
 *
 * Performance notes:
 * - No [BoxWithConstraints] — avoids sub-composition measurement
 * - No [Modifier.horizontalScroll] — avoids LazyColumn touch contention
 * - No [Modifier.height] with [IntrinsicSize] — avoids intrinsic measurement
 * - All [AnnotatedString]s cached via [remember] — no parse work on scroll
 * - [key] per row — stable composition identity across recompositions
 */
@Composable
fun CatppuccinMarkdownTable(
    content: String,
    node: ASTNode,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    val tableShape = RoundedCornerShape(8.dp)
    val annotatorSettings = annotatorSettings()

    // Pre-compute all cell AnnotatedStrings once — never re-parse during scroll
    val rows = remember(node, content, annotatorSettings) {
        node.children.mapNotNull { child ->
            when (child.type) {
                GFMElementTypes.HEADER -> {
                    val cells = child.children
                        .filter { it.type == GFMTokenTypes.CELL }
                        .map { cell ->
                            CachedCell(
                                annotatedText = content.buildMarkdownAnnotatedString(
                                    textNode = cell,
                                    style = style.copy(fontWeight = FontWeight.Bold),
                                    annotatorSettings = annotatorSettings,
                                ),
                                style = style.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                    CachedRow(RowType.HEADER, cells)
                }

                GFMElementTypes.ROW -> {
                    val cells = child.children
                        .filter { it.type == GFMTokenTypes.CELL }
                        .map { cell ->
                            CachedCell(
                                annotatedText = content.buildMarkdownAnnotatedString(
                                    textNode = cell,
                                    style = style,
                                    annotatorSettings = annotatorSettings,
                                ),
                                style = style,
                            )
                        }
                    CachedRow(RowType.ROW, cells)
                }

                GFMTokenTypes.TABLE_SEPARATOR -> CachedRow(RowType.SEPARATOR, emptyList())

                else -> null
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(0.5.dp, Surface1, tableShape)
            .clip(tableShape)
    ) {
        rows.forEachIndexed { index, row ->
            key(row.type.name + index) {
                when (row.type) {
                    RowType.HEADER -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Surface0)
                        ) {
                            row.cells.forEach { cell ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(0.5.dp, Surface1)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    MarkdownBasicText(
                                        text = cell.annotatedText,
                                        style = cell.style,
                                    )
                                }
                            }
                        }
                    }

                    RowType.ROW -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.cells.forEach { cell ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(0.5.dp, Surface1)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    MarkdownBasicText(
                                        text = cell.annotatedText,
                                        style = cell.style,
                                    )
                                }
                            }
                        }
                    }

                    RowType.SEPARATOR -> {
                        HorizontalDivider(thickness = 0.5.dp, color = Surface1)
                    }
                }
            }
        }
    }
}
