package com.zxxf.assistant.ui.chat.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.compose.components.markdownComponents
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

// ── Public API ─────────────────────────────────────────────────────────────

/**
 * Catppuccin-themed Markdown component overrides.
 *
 * The table component uses [HtmlGenerator] to convert the markdown table AST
 * to HTML, rendered in a transparent [WebView] with Catppuccin CSS. The browser
 * engine handles column sizing, text wrapping, and horizontal scroll natively.
 */
val catppuccinMarkdownComponents: MarkdownComponents = markdownComponents(
    table = { model ->
        MarkdownTableWebView(
            content = model.content,
            node = model.node,
        )
    }
)

// ── HTML table via WebView ─────────────────────────────────────────────────

/** Catppuccin Latte CSS injected into every table WebView. */
private val TABLE_CSS = """
body {
    font-family: -apple-system, sans-serif;
    font-size: 14px;
    color: #4c4f69;
    margin: 0;
    padding: 0;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
}
table {
    border-collapse: collapse;
    width: max-content;
    min-width: 100%;
}
th {
    background: #ccd0da;
    font-weight: bold;
    padding: 6px 10px;
    border: 0.5px solid #bcc0cc;
    white-space: nowrap;
}
td {
    padding: 6px 10px;
    border: 0.5px solid #bcc0cc;
}
""".trimIndent()

/** Initial height guess before JS measurement — generous enough to avoid flash. */
private val INITIAL_HEIGHT_DP = 200

/**
 * Renders a GFM table AST node as HTML inside a transparent [WebView].
 *
 * Column widths, text overflow, and horizontal scroll are all handled by the
 * browser engine — no Compose-level layout or measurement needed.
 *
 * Horizontal drags are intercepted at the Compose level so that swiping
 * across a wide table does not trigger [ModalNavigationDrawer].
 */
@Composable
fun MarkdownTableWebView(
    content: String,
    node: ASTNode,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    // Build HTML once — keyed on node identity + raw markdown text
    val html = remember(node, content) {
        val tableMarkdown = content.substring(node.startOffset, node.endOffset)
        val tableAst =
            MarkdownParser(GFMFlavourDescriptor()).buildMarkdownTreeFromString(tableMarkdown)
        val htmlGen = HtmlGenerator(tableMarkdown, tableAst, GFMFlavourDescriptor(), false)
        val tableHtml = htmlGen.generateHtml { _, _, attrs -> attrs }
        """
<!DOCTYPE html>
<html><head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<style>
$TABLE_CSS
</style></head><body>
$tableHtml
</body></html>
        """.trimIndent()
    }

    // Height: start generous, then measure exact size via JS after layout
    var tableHeight by remember { mutableStateOf(INITIAL_HEIGHT_DP.dp) }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                isVerticalFadingEdgeEnabled = false
                overScrollMode = WebView.OVER_SCROLL_NEVER

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        // Defer measurement until after the WebView has performed
                        // its first layout pass — onPageFinished fires before layout
                        // is complete for complex content (e.g. tables).
                        view.post {
                            measureTableHeight(view) { px ->
                                if (px > 0f) {
                                    tableHeight = with(density) { px.toDp() }
                                }
                            }
                        }
                        // Retry after a short delay for slow-rendering tables
                        view.postDelayed({
                            measureTableHeight(view) { px ->
                                if (px > 0f) {
                                    tableHeight = with(density) { px.toDp() }
                                }
                            }
                        }, 300)
                    }
                }
                loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        },
        modifier = modifier
            .fillMaxWidth()
            .height(tableHeight)
            .pointerInput(Unit) {
                // Consume horizontal drags so they don't propagate to
                // ModalNavigationDrawer and trigger the sidebar.
                detectHorizontalDragGestures { _, _ -> }
            }
    )
}

// ── Height measurement ─────────────────────────────────────────────────────

/**
 * Measures the full content height of [view] via JavaScript.
 *
 * Calls [onResult] with the measured height in **pixels**, or 0 if measurement
 * fails. Tries `document.body.scrollHeight` first, falls back to
 * `document.documentElement.scrollHeight` and the first child element.
 */
private fun measureTableHeight(view: WebView, onResult: (Float) -> Unit) {
    view.evaluateJavascript("""
        (function() {
            var h = document.body.scrollHeight;
            if (h > 0) return h;
            h = document.documentElement.scrollHeight;
            if (h > 0) return h;
            var first = document.body.firstElementChild;
            if (first) return first.scrollHeight;
            return 0;
        })()
    """.trimIndent()) { result ->
        val px = result?.trim('"')?.toFloatOrNull() ?: 0f
        onResult(px)
    }
}
