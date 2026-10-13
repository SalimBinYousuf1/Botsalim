package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.SalimCodeFontFamily
import com.example.ui.theme.SalimShape
import com.example.ui.theme.SalimTextStyles
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class MarkdownBlock {
    data class Paragraph(val text: String) : MarkdownBlock()
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class MathBlock(val latex: String) : MarkdownBlock()
    data class BlockQuote(val text: String) : MarkdownBlock()
    data class BulletList(val items: List<String>) : MarkdownBlock()
    data class NumberedList(val items: List<String>) : MarkdownBlock()
    data class Table(
        val headers: List<String>,
        val rows: List<List<String>>,
        val alignments: List<TextAlign> = emptyList()
    ) : MarkdownBlock()
    object Divider : MarkdownBlock()
}

/**
 * SalimMarkdownRenderer
 *
 * Implements Section M & User Spec:
 * 1. Two-typeface system: Serif display face (Lora) for headings H1/H2/H3, sans-serif (Plus Jakarta Sans) for body.
 * 2. Bold-term-then-explanation list pattern for bulleted and numbered lists.
 * 3. Zero leaked syntax with mid-stream incomplete formatting buffer.
 * 4. Full Markdown rendering everywhere: Bold, Italic, Bold-Italic, Inline Code, Math, Links, Strikethrough, Highlight.
 * 5. Markdown inside Tables: Headers and all data cells fully render all Markdown formatting.
 * 6. Charts & Diagrams: Interactive visual charts for data tables and Mermaid/Chart code blocks with fully rendered Markdown labels.
 * 7. Checklist / Task Lists: - [x] and - [ ] render real checkmark icons without leaked syntax.
 * 8. Generous vertical whitespace between paragraphs (14dp), list items (8dp), and headings (20dp top).
 * 9. Word-level highlighting during text-to-speech read aloud.
 */
@Composable
fun SalimMarkdownRenderer(
    content: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    isDark: Boolean = false,
    activeWordRange: Pair<Int, Int>? = null,
    isStreaming: Boolean = false,
    fontScale: Float = 1.0f
) {
    // Sanitize incomplete trailing markdown tokens during streaming to prevent syntax flicker
    val sanitizedContent = remember(content, isStreaming) {
        if (isStreaming) sanitizeIncompleteStreamingMarkdown(content) else content
    }

    val blocks = remember(sanitizedContent) { parseMarkdownBlocks(sanitizedContent) }

    SelectionContainer {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            blocks.forEach { block ->
                when (block) {
                    is MarkdownBlock.Heading -> {
                        val (style, topPadding, bottomPadding) = when (block.level) {
                            1 -> Triple(SalimTextStyles.SerifH1, 24.dp, 8.dp)
                            2 -> Triple(SalimTextStyles.SerifH2, 20.dp, 6.dp)
                            3 -> Triple(SalimTextStyles.SerifH3, 16.dp, 4.dp)
                            4 -> Triple(SalimTextStyles.SerifH3.copy(fontSize = 17.sp), 12.dp, 4.dp)
                            else -> Triple(SalimTextStyles.SerifH3.copy(fontSize = 15.sp), 10.dp, 2.dp)
                        }
                        Text(
                            text = buildAnnotatedInline(block.text, textColor, isDark, activeWordRange, content, isStreaming),
                            style = style.copy(fontSize = (style.fontSize.value * fontScale).sp),
                            color = textColor,
                            modifier = Modifier.padding(top = topPadding, bottom = bottomPadding)
                        )
                    }

                    is MarkdownBlock.Paragraph -> {
                        Text(
                            text = buildAnnotatedInline(block.text, textColor, isDark, activeWordRange, content, isStreaming),
                            style = SalimTextStyles.Body.copy(
                                fontSize = (17 * fontScale).sp,
                                lineHeight = (25 * fontScale).sp
                            ),
                            color = textColor
                        )
                    }

                    is MarkdownBlock.CodeBlock -> {
                        val lang = block.language.lowercase().trim()
                        if (lang in listOf("mermaid", "chart", "graph", "flowchart", "diagram")) {
                            RenderChartOrMermaidBlock(
                                language = lang,
                                code = block.code,
                                isDark = isDark,
                                fontScale = fontScale,
                                activeWordRange = activeWordRange,
                                fullContent = content
                            )
                        } else {
                            RenderCodeBlock(
                                language = block.language,
                                code = block.code,
                                isDark = isDark,
                                fontScale = fontScale
                            )
                        }
                    }

                    is MarkdownBlock.MathBlock -> {
                        RenderMathBlock(
                            latex = block.latex,
                            isDark = isDark,
                            fontScale = fontScale
                        )
                    }

                    is MarkdownBlock.BlockQuote -> {
                        RenderBlockQuote(
                            text = block.text,
                            textColor = textColor,
                            isDark = isDark,
                            fontScale = fontScale,
                            activeWordRange = activeWordRange,
                            fullContent = content,
                            isStreaming = isStreaming
                        )
                    }

                    is MarkdownBlock.BulletList -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            block.items.forEach { rawItem ->
                                val trimmed = rawItem.trim()
                                val isCheckedTask = trimmed.startsWith("[x] ", ignoreCase = true)
                                val isUncheckedTask = trimmed.startsWith("[ ] ")
                                val cleanItem = when {
                                    isCheckedTask || isUncheckedTask -> trimmed.substring(4).trim()
                                    else -> rawItem
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    if (isCheckedTask) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Completed",
                                            tint = AccentGreen,
                                            modifier = Modifier
                                                .padding(start = 2.dp, end = 8.dp, top = 2.dp)
                                                .size(18.dp)
                                        )
                                    } else if (isUncheckedTask) {
                                        Icon(
                                            imageVector = Icons.Default.RadioButtonUnchecked,
                                            contentDescription = "To do",
                                            tint = if (isDark) Color(0xFF8E968B) else Color(0xFF6B7268),
                                            modifier = Modifier
                                                .padding(start = 2.dp, end = 8.dp, top = 2.dp)
                                                .size(18.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "•",
                                            style = SalimTextStyles.Body.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = (17 * fontScale).sp
                                            ),
                                            color = if (isDark) AccentGold else AccentGreen,
                                            modifier = Modifier.padding(start = 2.dp, end = 10.dp)
                                        )
                                    }

                                    Text(
                                        text = formatBoldTermListInline(cleanItem, textColor, isDark, activeWordRange, content, isStreaming),
                                        style = SalimTextStyles.Body.copy(
                                            fontSize = (17 * fontScale).sp,
                                            lineHeight = (24 * fontScale).sp
                                        ),
                                        color = textColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    is MarkdownBlock.NumberedList -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            block.items.forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "${idx + 1}.",
                                        style = SalimTextStyles.Body.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = (17 * fontScale).sp
                                        ),
                                        color = if (isDark) AccentGold else AccentGreen,
                                        modifier = Modifier.padding(start = 2.dp, end = 8.dp)
                                    )
                                    Text(
                                        text = formatBoldTermListInline(item, textColor, isDark, activeWordRange, content, isStreaming),
                                        style = SalimTextStyles.Body.copy(
                                            fontSize = (17 * fontScale).sp,
                                            lineHeight = (24 * fontScale).sp
                                        ),
                                        color = textColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    is MarkdownBlock.Table -> {
                        RenderTable(
                            headers = block.headers,
                            rows = block.rows,
                            alignments = block.alignments,
                            isDark = isDark,
                            fontScale = fontScale,
                            activeWordRange = activeWordRange,
                            fullContent = content
                        )
                    }

                    is MarkdownBlock.Divider -> {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            thickness = 1.dp,
                            color = if (isDark) Color(0xFF2C322C) else Color(0xFFD6DAD4)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bold-term-then-explanation list pattern (Section M.3)
 */
private fun formatBoldTermListInline(
    itemText: String,
    baseColor: Color,
    isDark: Boolean,
    activeWordRange: Pair<Int, Int>?,
    fullContent: String,
    isStreaming: Boolean = false
): AnnotatedString {
    // Case 1: Item already has explicit bold at the beginning: "**Term**: rest" or "**Term** — rest"
    val boldLeadRegex = Regex("^\\*\\*(.+?)\\*\\*([:\\s—–-]*)(.*)", RegexOption.DOT_MATCHES_ALL)
    val boldLeadMatch = boldLeadRegex.find(itemText)
    if (boldLeadMatch != null) {
        val term = boldLeadMatch.groupValues[1].trim()
        val rest = boldLeadMatch.groupValues[3].trim()
        val separator = if (boldLeadMatch.groupValues[2].contains(":")) ": " else " — "
        val reformatted = "**$term**$separator$rest"
        return buildAnnotatedInline(reformatted, baseColor, isDark, activeWordRange, fullContent, isStreaming)
    }

    // Case 2: Item has "Term: Explanation" or "Term — Explanation" without existing bold marks
    val termColonRegex = Regex("^([^:—–\n]{2,45})([:—–])\\s*(.+)", RegexOption.DOT_MATCHES_ALL)
    val termMatch = termColonRegex.find(itemText)
    if (termMatch != null) {
        val term = termMatch.groupValues[1].trim()
        val sep = termMatch.groupValues[2]
        val rest = termMatch.groupValues[3].trim()
        val normalizedSep = if (sep == ":") ": " else " — "
        val reformatted = "**$term**$normalizedSep$rest"
        return buildAnnotatedInline(reformatted, baseColor, isDark, activeWordRange, fullContent, isStreaming)
    }

    // Fallback: standard inline formatting
    return buildAnnotatedInline(itemText, baseColor, isDark, activeWordRange, fullContent, isStreaming)
}

/**
 * Mid-stream sanitization: holds back incomplete formatting tokens at the end of the text.
 */
private fun sanitizeIncompleteStreamingMarkdown(text: String): String {
    var s = text.trimEnd()
    while (s.endsWith("***") || s.endsWith("**") || s.endsWith("```") || s.endsWith("~~") || s.endsWith("__")) {
        if (s.endsWith("***")) s = s.dropLast(3)
        if (s.endsWith("```")) s = s.dropLast(3)
        if (s.endsWith("**")) s = s.dropLast(2)
        if (s.endsWith("~~")) s = s.dropLast(2)
        if (s.endsWith("__")) s = s.dropLast(2)
    }
    if (s.endsWith("*") && !s.endsWith(" *")) s = s.dropLast(1)
    if (s.endsWith("_") && !s.endsWith(" _")) s = s.dropLast(1)
    if (s.endsWith("`") && !s.endsWith(" `")) s = s.dropLast(1)
    if (s.endsWith("#")) s = s.dropLast(1)
    if (s.endsWith("[")) s = s.dropLast(1)
    return s
}

@Composable
private fun RenderCodeBlock(
    language: String,
    code: String,
    isDark: Boolean,
    fontScale: Float
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val codeBg = if (isDark) Color(0xFF141715) else Color(0xFF202421)
    val headerBg = if (isDark) Color(0xFF0F110F) else Color(0xFF181B18)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SalimShape)
            .background(codeBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBg)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.ifBlank { "code" }.lowercase(),
                style = SalimTextStyles.Caption.copy(fontFamily = SalimCodeFontFamily),
                color = Color(0xFFB5BEB4)
            )

            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("code", code))
                    copied = true
                    scope.launch {
                        delay(2000)
                        copied = false
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = if (copied) "Copied" else "Copy code",
                    tint = if (copied) AccentGreen else Color(0xFFB5BEB4),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(14.dp)
        ) {
            Text(
                text = code,
                style = SalimTextStyles.Code.copy(fontSize = (14 * fontScale).sp),
                color = Color(0xFFF1F5F0)
            )
        }
    }
}

/**
 * RenderChartOrMermaidBlock
 * Renders visual diagram / chart presentation for Mermaid or Chart code blocks
 * with toggle for raw code view. Labels are rendered using Markdown!
 */
@Composable
private fun RenderChartOrMermaidBlock(
    language: String,
    code: String,
    isDark: Boolean,
    fontScale: Float,
    activeWordRange: Pair<Int, Int>? = null,
    fullContent: String = ""
) {
    var viewMode by remember { mutableStateOf("visual") } // "visual" or "code"
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val cardBg = if (isDark) Color(0xFF171B17) else Color(0xFFF1F5EE)
    val headerBg = if (isDark) Color(0xFF111411) else Color(0xFFE4EAE0)
    val titleColor = if (isDark) Color(0xFFE5EAE2) else Color(0xFF1B231D)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SalimShape)
            .background(cardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBg)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = if (isDark) AccentGold else AccentGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${language.uppercase()} Chart / Diagram",
                    style = SalimTextStyles.Caption.copy(fontWeight = FontWeight.Bold),
                    color = titleColor
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDark) Color(0xFF232823) else Color(0xFFD6DED2))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (viewMode == "visual") (if (isDark) AccentGold.copy(alpha = 0.2f) else AccentGreen.copy(alpha = 0.2f)) else Color.Transparent)
                            .clickable { viewMode = "visual" }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Visual",
                            style = SalimTextStyles.Caption.copy(
                                fontSize = 11.sp,
                                fontWeight = if (viewMode == "visual") FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (viewMode == "visual") (if (isDark) AccentGold else AccentGreen) else (if (isDark) Color(0xFFB0B8AE) else Color(0xFF6B7569))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (viewMode == "code") (if (isDark) AccentGold.copy(alpha = 0.2f) else AccentGreen.copy(alpha = 0.2f)) else Color.Transparent)
                            .clickable { viewMode = "code" }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Code",
                            style = SalimTextStyles.Caption.copy(
                                fontSize = 11.sp,
                                fontWeight = if (viewMode == "code") FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (viewMode == "code") (if (isDark) AccentGold else AccentGreen) else (if (isDark) Color(0xFFB0B8AE) else Color(0xFF6B7569))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("diagram", code))
                        copied = true
                        scope.launch {
                            delay(2000)
                            copied = false
                        }
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = if (copied) AccentGreen else Color(0xFFB5BEB4),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        if (viewMode == "visual") {
            RenderMermaidOrChartVisual(
                code = code,
                isDark = isDark,
                fontScale = fontScale,
                activeWordRange = activeWordRange,
                fullContent = fullContent
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                Text(
                    text = code,
                    style = SalimTextStyles.Code.copy(fontSize = (14 * fontScale).sp),
                    color = if (isDark) Color(0xFFF1F5F0) else Color(0xFF222622)
                )
            }
        }
    }
}

/**
 * Parses and visualizes Mermaid or Chart structures (pie, graph/flowchart, sequence, or bar data)
 * with fully rendered Markdown labels.
 */
@Composable
private fun RenderMermaidOrChartVisual(
    code: String,
    isDark: Boolean,
    fontScale: Float,
    activeWordRange: Pair<Int, Int>? = null,
    fullContent: String = ""
) {
    val lines = code.lines().map { it.trim() }.filter { it.isNotBlank() && !it.startsWith("%%") }
    val isPie = lines.any { it.startsWith("pie", ignoreCase = true) }
    val isGraph = lines.any { it.startsWith("graph", ignoreCase = true) || it.startsWith("flowchart", ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (isPie) {
            // Render Pie / Distribution Breakdown
            val titleLine = lines.firstOrNull { it.startsWith("pie", ignoreCase = true) } ?: "pie"
            val pieTitle = titleLine.substringAfter("title", "").trim()
            if (pieTitle.isNotBlank()) {
                Text(
                    text = buildAnnotatedInline(pieTitle, if (isDark) Color(0xFFE5EAE2) else Color(0xFF1E2420), isDark, activeWordRange, fullContent),
                    style = SalimTextStyles.Title.copy(fontSize = (16 * fontScale).sp),
                    color = if (isDark) Color(0xFFE5EAE2) else Color(0xFF1E2420)
                )
            }

            val items = mutableListOf<Pair<String, Double>>()
            lines.drop(1).forEach { line ->
                if (line.contains(":")) {
                    val labelPart = line.substringBefore(":").trim().removeSurrounding("\"").trim()
                    val valPart = line.substringAfter(":").trim().toDoubleOrNull() ?: 0.0
                    if (labelPart.isNotBlank()) items.add(labelPart to valPart)
                }
            }

            val total = items.sumOf { it.second }.coerceAtLeast(0.001)
            val colors = listOf(AccentGreen, AccentGold, Color(0xFF43A047), Color(0xFF1E88E5), Color(0xFFFB8C00), Color(0xFF8E24AA))

            items.forEachIndexed { idx, (label, value) ->
                val fraction = (value / total).toFloat().coerceIn(0.02f, 1f)
                val pct = (fraction * 100).toInt()
                val color = colors[idx % colors.size]

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = buildAnnotatedInline(label, if (isDark) Color(0xFFE5EAE2) else Color(0xFF1E2420), isDark, activeWordRange, fullContent),
                                style = SalimTextStyles.Secondary.copy(fontWeight = FontWeight.Medium, fontSize = (14 * fontScale).sp)
                            )
                        }
                        Text(
                            text = "$pct% ($value)",
                            style = SalimTextStyles.Code.copy(fontWeight = FontWeight.Bold, fontSize = (13 * fontScale).sp),
                            color = if (isDark) Color(0xFFB5BEB4) else Color(0xFF4C5549)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (isDark) Color(0xFF262C26) else Color(0xFFE0E5DC))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(color)
                        )
                    }
                }
            }
        } else if (isGraph) {
            // Render Visual Flowchart nodes & connections
            Text(
                text = "Flowchart Steps & Transitions",
                style = SalimTextStyles.Caption.copy(fontWeight = FontWeight.Bold),
                color = if (isDark) AccentGold else AccentGreen
            )

            val edgeRegex = Regex("([a-zA-Z0-9_\\[\\]\\(\\)\\{\\}\"\\s]+)\\s*(-->|---|==>|-.->)\\s*([a-zA-Z0-9_\\[\\]\\(\\)\\{\\}\"\\s]+)")
            val connections = mutableListOf<Triple<String, String, String>>()

            lines.drop(1).forEach { line ->
                val match = edgeRegex.find(line)
                if (match != null) {
                    val from = cleanNodeName(match.groupValues[1])
                    val arrow = match.groupValues[2]
                    val to = cleanNodeName(match.groupValues[3])
                    connections.add(Triple(from, arrow, to))
                }
            }

            if (connections.isNotEmpty()) {
                connections.forEachIndexed { idx, (from, _, to) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDark) Color(0xFF212621) else Color(0xFFE6EBE1))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = buildAnnotatedInline(from, if (isDark) Color(0xFFE5EAE2) else Color(0xFF1E2420), isDark, activeWordRange, fullContent),
                                style = SalimTextStyles.Secondary.copy(fontWeight = FontWeight.Bold, fontSize = (13 * fontScale).sp)
                            )
                        }

                        Text(
                            text = "  ➔  ",
                            color = if (isDark) AccentGold else AccentGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isDark) Color(0xFF2B332B) else Color(0xFFD6DEC2))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = buildAnnotatedInline(to, if (isDark) Color(0xFFE5EAE2) else Color(0xFF1E2420), isDark, activeWordRange, fullContent),
                                style = SalimTextStyles.Secondary.copy(fontWeight = FontWeight.Medium, fontSize = (13 * fontScale).sp)
                            )
                        }
                    }
                }
            } else {
                // Freeform graph representation
                lines.forEach { l ->
                    Text(
                        text = buildAnnotatedInline(l, if (isDark) Color(0xFFD6DBD3) else Color(0xFF2C352E), isDark, activeWordRange, fullContent),
                        style = SalimTextStyles.Secondary.copy(fontSize = (14 * fontScale).sp)
                    )
                }
            }
        } else {
            // General Key-Value or Sequence chart
            lines.forEach { l ->
                Text(
                    text = buildAnnotatedInline(l, if (isDark) Color(0xFFD6DBD3) else Color(0xFF2C352E), isDark, activeWordRange, fullContent),
                    style = SalimTextStyles.Secondary.copy(fontSize = (14 * fontScale).sp)
                )
            }
        }
    }
}

private fun cleanNodeName(raw: String): String {
    return raw.trim()
        .replace("[", "")
        .replace("]", "")
        .replace("(", "")
        .replace(")", "")
        .replace("{", "")
        .replace("}", "")
        .replace("\"", "")
        .trim()
}

@Composable
private fun RenderMathBlock(
    latex: String,
    isDark: Boolean,
    fontScale: Float
) {
    val mathBg = if (isDark) Color(0xFF1E211E) else Color(0xFFF0F3ED)
    val formattedMath = formatLatexToReadable(latex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SalimShape)
            .background(mathBg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formattedMath,
            style = SalimTextStyles.Body.copy(
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                fontSize = (17 * fontScale).sp
            ),
            color = if (isDark) Color(0xFFD6DBD3) else Color(0xFF263028)
        )
    }
}

@Composable
private fun RenderBlockQuote(
    text: String,
    textColor: Color,
    isDark: Boolean,
    fontScale: Float,
    activeWordRange: Pair<Int, Int>?,
    fullContent: String,
    isStreaming: Boolean = false
) {
    val barColor = if (isDark) AccentGold else AccentGreen

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(30.dp)
                .clip(CircleShape)
                .background(barColor)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = buildAnnotatedInline(text, textColor, isDark, activeWordRange, fullContent, isStreaming),
            style = SalimTextStyles.Body.copy(
                fontStyle = FontStyle.Italic,
                fontSize = (17 * fontScale).sp
            ),
            color = textColor.copy(alpha = 0.9f)
        )
    }
}

/**
 * RenderTable
 * Renders Markdown tables with:
 * 1. Full Markdown parsing in every header and data cell! (bold, italic, code, math, links, tags)
 * 2. Adaptive column width sizing.
 * 3. Text alignment matching Markdown separator row (:---:, ---:, :---).
 * 4. Automatic "Chart View" toggle when numerical data is present in table rows.
 */
@Composable
private fun RenderTable(
    headers: List<String>,
    rows: List<List<String>>,
    alignments: List<TextAlign>,
    isDark: Boolean,
    fontScale: Float,
    activeWordRange: Pair<Int, Int>? = null,
    fullContent: String = ""
) {
    val tableBg = if (isDark) Color(0xFF1B1E1B) else Color(0xFFF4F6F2)
    val headerBg = if (isDark) Color(0xFF151815) else Color(0xFFE9EEE5)
    val borderColor = if (isDark) Color(0xFF2C322C) else Color(0xFFDDE2DA)
    val headerTextColor = if (isDark) Color(0xFFE5EAE2) else Color(0xFF1E2420)
    val cellTextColor = if (isDark) Color(0xFFC8CEC5) else Color(0xFF333B35)

    // Detect if table has numeric columns for visual chart rendering
    val numericColIdx = remember(headers, rows) {
        if (rows.size >= 2) {
            (1 until headers.size).firstOrNull { col ->
                val numericCount = rows.count { row ->
                    val cell = row.getOrNull(col) ?: ""
                    parseNumericValue(cell) != null
                }
                numericCount >= (rows.size * 0.5)
            }
        } else null
    }

    val hasChartOption = numericColIdx != null
    var activeViewMode by remember { mutableStateOf("table") }

    // Adaptive column widths
    val colWidths = remember(headers, rows) {
        headers.indices.map { col ->
            val headerLen = headers.getOrNull(col)?.length ?: 0
            val maxRowLen = rows.maxOfOrNull { it.getOrNull(col)?.length ?: 0 } ?: 0
            val maxChars = maxOf(headerLen, maxRowLen)
            when {
                maxChars <= 5 -> 80.dp
                maxChars <= 12 -> 120.dp
                maxChars <= 22 -> 175.dp
                else -> 230.dp
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SalimShape)
            .background(tableBg)
            .padding(10.dp)
    ) {
        // Toggle bar if table can be viewed as chart
        if (hasChartOption) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (activeViewMode == "chart") Icons.Default.BarChart else Icons.Default.TableChart,
                        contentDescription = null,
                        tint = if (isDark) AccentGold else AccentGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (activeViewMode == "chart") "Interactive Data Chart" else "Structured Table",
                        style = SalimTextStyles.Caption.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isDark) Color(0xFFD6DBD3) else Color(0xFF2C352E)
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0xFF252925) else Color(0xFFE2E7DE))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeViewMode == "table") (if (isDark) AccentGold.copy(alpha = 0.2f) else AccentGreen.copy(alpha = 0.2f)) else Color.Transparent)
                            .clickable { activeViewMode = "table" }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Table",
                            style = SalimTextStyles.Caption.copy(
                                fontWeight = if (activeViewMode == "table") FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            ),
                            color = if (activeViewMode == "table") (if (isDark) AccentGold else AccentGreen) else (if (isDark) Color(0xFFB0B8AE) else Color(0xFF6B7569))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeViewMode == "chart") (if (isDark) AccentGold.copy(alpha = 0.2f) else AccentGreen.copy(alpha = 0.2f)) else Color.Transparent)
                            .clickable { activeViewMode = "chart" }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Chart",
                            style = SalimTextStyles.Caption.copy(
                                fontWeight = if (activeViewMode == "chart") FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            ),
                            color = if (activeViewMode == "chart") (if (isDark) AccentGold else AccentGreen) else (if (isDark) Color(0xFFB0B8AE) else Color(0xFF6B7569))
                        )
                    }
                }
            }
        }

        if (activeViewMode == "chart" && numericColIdx != null) {
            RenderTableChart(
                headers = headers,
                rows = rows,
                numericCol = numericColIdx,
                isDark = isDark,
                fontScale = fontScale,
                activeWordRange = activeWordRange,
                fullContent = fullContent
            )
        } else {
            // Render Table with full Markdown in every cell
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Column {
                    // Header row
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(headerBg)
                            .padding(vertical = 6.dp)
                    ) {
                        headers.forEachIndexed { colIdx, header ->
                            val width = colWidths.getOrElse(colIdx) { 140.dp }
                            val align = alignments.getOrElse(colIdx) { TextAlign.Start }
                            val cleanHeader = header.trim().replace("<br>", "\n").replace("<br/>", "\n")
                            Box(
                                modifier = Modifier
                                    .width(width)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = buildAnnotatedInline(
                                        cleanHeader,
                                        headerTextColor,
                                        isDark,
                                        activeWordRange,
                                        fullContent
                                    ),
                                    style = SalimTextStyles.Secondary.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = (15 * fontScale).sp
                                    ),
                                    textAlign = align,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    HorizontalDivider(thickness = 1.5.dp, color = borderColor)

                    // Data rows
                    rows.forEachIndexed { rowIdx, row ->
                        val rowBg = if (rowIdx % 2 == 1) (if (isDark) Color(0xFF222622) else Color(0xFFECEFE8)) else Color.Transparent
                        Row(
                            modifier = Modifier
                                .background(rowBg)
                                .padding(vertical = 6.dp)
                        ) {
                            headers.indices.forEach { colIdx ->
                                val cell = row.getOrNull(colIdx) ?: ""
                                val width = colWidths.getOrElse(colIdx) { 140.dp }
                                val align = alignments.getOrElse(colIdx) { TextAlign.Start }
                                val cleanCell = cell.trim().replace("<br>", "\n").replace("<br/>", "\n")
                                Box(
                                    modifier = Modifier
                                        .width(width)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = buildAnnotatedInline(
                                            cleanCell,
                                            cellTextColor,
                                            isDark,
                                            activeWordRange,
                                            fullContent
                                        ),
                                        style = SalimTextStyles.Secondary.copy(fontSize = (15 * fontScale).sp),
                                        textAlign = align,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                        if (rowIdx < rows.lastIndex) {
                            HorizontalDivider(thickness = 0.5.dp, color = borderColor.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * RenderTableChart
 * Visualizes table data into a bar chart with full Markdown label support.
 */
@Composable
private fun RenderTableChart(
    headers: List<String>,
    rows: List<List<String>>,
    numericCol: Int,
    isDark: Boolean,
    fontScale: Float,
    activeWordRange: Pair<Int, Int>? = null,
    fullContent: String = ""
) {
    val metricHeader = headers.getOrNull(numericCol) ?: "Metric"
    val rowValues = rows.map { row ->
        val label = row.getOrNull(0) ?: ""
        val cellStr = row.getOrNull(numericCol) ?: "0"
        val num = parseNumericValue(cellStr) ?: 0.0
        Triple(label, cellStr, num)
    }

    val maxVal = rowValues.maxOfOrNull { it.third }?.coerceAtLeast(0.001) ?: 1.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Metric: $metricHeader",
            style = SalimTextStyles.Caption.copy(fontWeight = FontWeight.Bold),
            color = if (isDark) AccentGold else AccentGreen
        )

        rowValues.forEachIndexed { idx, (label, cellStr, num) ->
            val fraction = (num / maxVal).toFloat().coerceIn(0.04f, 1f)
            val barColor = if (idx % 2 == 0) {
                if (isDark) AccentGold else AccentGreen
            } else {
                if (isDark) AccentGreen else Color(0xFF2E7D32)
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildAnnotatedInline(
                            label.ifEmpty { "Row ${idx + 1}" },
                            if (isDark) Color(0xFFE5EAE2) else Color(0xFF1E2420),
                            isDark,
                            activeWordRange,
                            fullContent
                        ),
                        style = SalimTextStyles.Secondary.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = (14 * fontScale).sp
                        ),
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = buildAnnotatedInline(
                            cellStr,
                            if (isDark) Color(0xFFE5EAE2) else Color(0xFF1E2420),
                            isDark,
                            activeWordRange,
                            fullContent
                        ),
                        style = SalimTextStyles.Code.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = (13 * fontScale).sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (isDark) Color(0xFF252A25) else Color(0xFFE0E5DC))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(barColor)
                    )
                }
            }
        }
    }
}

private fun parseNumericValue(cell: String): Double? {
    val cleaned = cell.trim()
        .replace("$", "")
        .replace("€", "")
        .replace("£", "")
        .replace("¥", "")
        .replace("%", "")
        .replace(",", "")
        .replace("+", "")
        .trim()
    return cleaned.toDoubleOrNull()
}

/**
 * Parses markdown lines into semantic blocks.
 */
private fun parseMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = rawText.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Fenced code block
        if (line.trimStart().startsWith("```")) {
            val language = line.trimStart().removePrefix("```").trim()
            val codeBuilder = StringBuilder()
            i++
            while (i < lines.size) {
                if (lines[i].trimStart().startsWith("```")) {
                    i++
                    break
                }
                codeBuilder.append(lines[i]).append("\n")
                i++
            }
            val codeText = codeBuilder.toString().removeSuffix("\n")
            blocks.add(MarkdownBlock.CodeBlock(language, codeText))
            continue
        }

        // Block math: $$ ... $$
        if (line.trim().startsWith("$$")) {
            val mathBuilder = StringBuilder()
            val singleLine = line.trim().removePrefix("$$").removeSuffix("$$").trim()
            if (line.trim().endsWith("$$") && line.trim().length > 4) {
                blocks.add(MarkdownBlock.MathBlock(singleLine))
                i++
                continue
            }
            i++
            while (i < lines.size) {
                if (lines[i].trim().endsWith("$$")) {
                    mathBuilder.append(lines[i].trim().removeSuffix("$$"))
                    i++
                    break
                }
                mathBuilder.append(lines[i]).append(" ")
                i++
            }
            blocks.add(MarkdownBlock.MathBlock(mathBuilder.toString().trim()))
            continue
        }

        // Horizontal rules
        if (line.trim() in listOf("---", "***", "___")) {
            blocks.add(MarkdownBlock.Divider)
            i++
            continue
        }

        // Headings (H1 through H6)
        val headingMatch = Regex("^(#{1,6})\\s+(.*)").find(line)
        if (headingMatch != null) {
            val level = headingMatch.groupValues[1].length
            val headingText = headingMatch.groupValues[2].trim()
            blocks.add(MarkdownBlock.Heading(level, headingText))
            i++
            continue
        }

        // Blockquotes
        if (line.startsWith(">")) {
            val quoteBuilder = StringBuilder(line.removePrefix(">").trim())
            i++
            while (i < lines.size && lines[i].startsWith(">")) {
                quoteBuilder.append(" ").append(lines[i].removePrefix(">").trim())
                i++
            }
            blocks.add(MarkdownBlock.BlockQuote(quoteBuilder.toString()))
            continue
        }

        // Bullet lists and Task lists
        if (line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ")) {
            val items = mutableListOf<String>()
            while (i < lines.size && (lines[i].trimStart().startsWith("- ") || lines[i].trimStart().startsWith("* "))) {
                items.add(lines[i].trimStart().substring(2).trim())
                i++
            }
            blocks.add(MarkdownBlock.BulletList(items))
            continue
        }

        // Numbered lists
        val numRegex = Regex("^(\\d+)\\.\\s+(.*)")
        if (numRegex.matches(line.trimStart())) {
            val items = mutableListOf<String>()
            while (i < lines.size && numRegex.matches(lines[i].trimStart())) {
                val match = numRegex.find(lines[i].trimStart())
                items.add(match?.groupValues?.get(2) ?: lines[i])
                i++
            }
            blocks.add(MarkdownBlock.NumberedList(items))
            continue
        }

        // Markdown Table
        if (line.contains("|") && i + 1 < lines.size && lines[i + 1].contains("---")) {
            val headerCells = parseTableRow(line)
            val sepLine = lines[i + 1]
            val alignments = parseTableAlignments(sepLine)
            i += 2
            val rows = mutableListOf<List<String>>()
            while (i < lines.size && lines[i].contains("|")) {
                val rowCells = parseTableRow(lines[i])
                if (rowCells.isNotEmpty() && !rowCells.all { it.isBlank() }) {
                    rows.add(rowCells)
                }
                i++
            }
            if (headerCells.isNotEmpty()) {
                blocks.add(MarkdownBlock.Table(headerCells, rows, alignments))
            }
            continue
        }

        // Blank lines
        if (line.isBlank()) {
            i++
            continue
        }

        // Regular paragraph
        val paragraphBuilder = StringBuilder(line)
        i++
        while (i < lines.size && lines[i].isNotBlank() &&
            !lines[i].startsWith("#") &&
            !lines[i].startsWith("```") &&
            !lines[i].startsWith(">") &&
            !lines[i].startsWith("- ") &&
            !lines[i].startsWith("* ") &&
            !lines[i].contains("|") &&
            !numRegex.matches(lines[i].trimStart())
        ) {
            paragraphBuilder.append("\n").append(lines[i])
            i++
        }
        blocks.add(MarkdownBlock.Paragraph(paragraphBuilder.toString()))
    }

    return blocks
}

private fun parseTableRow(rawLine: String): List<String> {
    var line = rawLine.trim()
    if (line.startsWith("|")) line = line.substring(1)
    if (line.endsWith("|")) line = line.substring(0, line.length - 1)
    // Protect escaped pipes \|
    val placeholder = "@@PIPE@@"
    val safeLine = line.replace("\\|", placeholder)
    return safeLine.split("|").map { it.replace(placeholder, "|").trim() }
}

private fun parseTableAlignments(sepLine: String): List<TextAlign> {
    val cells = parseTableRow(sepLine)
    return cells.map { cell ->
        val trimmed = cell.trim()
        val startsColon = trimmed.startsWith(":")
        val endsColon = trimmed.endsWith(":")
        when {
            startsColon && endsColon -> TextAlign.Center
            endsColon -> TextAlign.End
            else -> TextAlign.Start
        }
    }
}

/**
 * Builds annotated string for inline tokens:
 * - Bold + Italic: ***text*** and ___text___
 * - Bold: **text** and __text__
 * - Italic: *text* and _text_
 * - Strikethrough: ~~text~~
 * - Highlight: ==text==
 * - Inline Code: `code`
 * - Inline Math: $formula$
 * - Links: [label](url)
 * - HTML entities decoding (&amp;, &lt;, &gt;, &quot;, &#39;)
 * - Line breaks: <br> and <br/>
 * - Word-level highlighting during TTS
 */
fun buildAnnotatedInline(
    rawText: String,
    baseColor: Color,
    isDark: Boolean,
    activeWordRange: Pair<Int, Int>? = null,
    fullContent: String = "",
    isStreaming: Boolean = false
): AnnotatedString {
    // Pre-decode common HTML entities and line breaks
    val text = rawText
        .replace("<br>", "\n")
        .replace("<br/>", "\n")
        .replace("<br />", "\n")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")

    return buildAnnotatedString {
        var cursor = 0
        val len = text.length

        while (cursor < len) {
            // Inline code: `code`
            if (text[cursor] == '`') {
                val end = text.indexOf('`', cursor + 1)
                if (end != -1) {
                    val codeSpan = text.substring(cursor + 1, end)
                    withStyle(
                        SpanStyle(
                            fontFamily = SalimCodeFontFamily,
                            background = if (isDark) Color(0xFF2B302B) else Color(0xFFE2E7DE),
                            color = if (isDark) Color(0xFFF1F5F0) else Color(0xFF1E221E),
                            fontSize = 14.sp
                        )
                    ) {
                        append(" $codeSpan ")
                    }
                    cursor = end + 1
                    continue
                } else if (isStreaming) {
                    val codeSpan = text.substring(cursor + 1)
                    withStyle(
                        SpanStyle(
                            fontFamily = SalimCodeFontFamily,
                            background = if (isDark) Color(0xFF2B302B) else Color(0xFFE2E7DE),
                            color = if (isDark) Color(0xFFF1F5F0) else Color(0xFF1E221E),
                            fontSize = 14.sp
                        )
                    ) {
                        append(" $codeSpan")
                    }
                    break
                }
            }

            // Inline math: $formula$
            if (text[cursor] == '$' && cursor + 1 < len && text[cursor + 1] != '$') {
                val end = text.indexOf('$', cursor + 1)
                if (end != -1) {
                    val mathSpan = text.substring(cursor + 1, end)
                    val readable = formatLatexToReadable(mathSpan)
                    withStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) AccentGold else AccentGreen
                        )
                    ) {
                        append(readable)
                    }
                    cursor = end + 1
                    continue
                }
            }

            // Markdown Links: [label](url)
            if (text[cursor] == '[') {
                val closeBracket = text.indexOf(']', cursor + 1)
                if (closeBracket != -1 && closeBracket + 1 < len && text[closeBracket + 1] == '(') {
                    val closeParen = text.indexOf(')', closeBracket + 2)
                    if (closeParen != -1) {
                        val label = text.substring(cursor + 1, closeBracket)
                        val url = text.substring(closeBracket + 2, closeParen)
                        pushStringAnnotation(tag = "URL", annotation = url)
                        withStyle(
                            SpanStyle(
                                color = if (isDark) AccentGold else AccentGreen,
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Medium
                            )
                        ) {
                            append(label)
                        }
                        pop()
                        cursor = closeParen + 1
                        continue
                    }
                } else if (isStreaming && closeBracket == -1) {
                    val partialLabel = text.substring(cursor + 1)
                    append(partialLabel)
                    break
                }
            }

            // Highlight: ==text==
            if (cursor + 1 < len && text.substring(cursor, cursor + 2) == "==") {
                val end = text.indexOf("==", cursor + 2)
                if (end != -1) {
                    val hlSpan = text.substring(cursor + 2, end)
                    withStyle(
                        SpanStyle(
                            background = if (isDark) AccentGold.copy(alpha = 0.25f) else Color(0xFFFFF3CD),
                            color = baseColor,
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append(hlSpan)
                    }
                    cursor = end + 2
                    continue
                }
            }

            // Bold + Italic: ***text*** or ___text___
            if (cursor + 2 < len && (text.substring(cursor, cursor + 3) == "***" || text.substring(cursor, cursor + 3) == "___")) {
                val delim = text.substring(cursor, cursor + 3)
                val end = text.indexOf(delim, cursor + 3)
                if (end != -1) {
                    val boldItalicSpan = text.substring(cursor + 3, end)
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            color = baseColor
                        )
                    ) {
                        append(boldItalicSpan)
                    }
                    cursor = end + 3
                    continue
                } else if (isStreaming) {
                    val boldItalicSpan = text.substring(cursor + 3)
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            color = baseColor
                        )
                    ) {
                        append(boldItalicSpan)
                    }
                    break
                }
            }

            // Bold: **text** or __text__
            if (cursor + 1 < len && (text.substring(cursor, cursor + 2) == "**" || text.substring(cursor, cursor + 2) == "__")) {
                val delim = text.substring(cursor, cursor + 2)
                val end = text.indexOf(delim, cursor + 2)
                if (end != -1) {
                    val boldSpan = text.substring(cursor + 2, end)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor)) {
                        append(boldSpan)
                    }
                    cursor = end + 2
                    continue
                } else if (isStreaming) {
                    val boldSpan = text.substring(cursor + 2)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor)) {
                        append(boldSpan)
                    }
                    break
                }
            }

            // Strikethrough: ~~text~~
            if (cursor + 1 < len && text.substring(cursor, cursor + 2) == "~~") {
                val end = text.indexOf("~~", cursor + 2)
                if (end != -1) {
                    val strikeSpan = text.substring(cursor + 2, end)
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = baseColor.copy(alpha = 0.7f))) {
                        append(strikeSpan)
                    }
                    cursor = end + 2
                    continue
                } else if (isStreaming) {
                    val strikeSpan = text.substring(cursor + 2)
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = baseColor.copy(alpha = 0.7f))) {
                        append(strikeSpan)
                    }
                    break
                }
            }

            // Italic: *text* or _text_
            if ((text[cursor] == '*' || text[cursor] == '_') && (cursor + 1 < len && text[cursor + 1] != text[cursor])) {
                val delim = text[cursor]
                val end = text.indexOf(delim, cursor + 1)
                if (end != -1 && end > cursor + 1) {
                    val italicSpan = text.substring(cursor + 1, end)
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = baseColor)) {
                        append(italicSpan)
                    }
                    cursor = end + 1
                    continue
                } else if (isStreaming) {
                    val italicSpan = text.substring(cursor + 1)
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = baseColor)) {
                        append(italicSpan)
                    }
                    break
                }
            }

            // Word-level highlighting during TTS
            val charPosInFull = if (fullContent.isNotEmpty()) fullContent.indexOf(text) + cursor else -1
            val isSpokenNow = activeWordRange != null && charPosInFull >= 0 &&
                    charPosInFull >= activeWordRange.first && charPosInFull < activeWordRange.second

            if (isSpokenNow) {
                withStyle(
                    SpanStyle(
                        background = if (isDark) AccentGold.copy(alpha = 0.4f) else AccentGreen.copy(alpha = 0.3f),
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append(text[cursor])
                }
            } else {
                append(text[cursor])
            }
            cursor++
        }
    }
}

private fun formatLatexToReadable(latex: String): String {
    return latex
        .replace("\\frac{", "(")
        .replace("}{", " / ")
        .replace("}", ")")
        .replace("\\sqrt{", "√(")
        .replace("\\sum", "∑")
        .replace("\\int", "∫")
        .replace("\\infty", "∞")
        .replace("\\times", "×")
        .replace("\\div", "÷")
        .replace("\\pm", "±")
        .replace("\\alpha", "α")
        .replace("\\beta", "β")
        .replace("\\gamma", "γ")
        .replace("\\delta", "δ")
        .replace("\\pi", "π")
        .replace("\\theta", "θ")
        .replace("\\lambda", "λ")
        .replace("\\mu", "μ")
        .replace("\\sigma", "σ")
        .replace("\\le", "≤")
        .replace("\\ge", "≥")
        .replace("\\neq", "≠")
        .replace("\\approx", "≈")
        .replace("^2", "²")
        .replace("^3", "³")
        .replace("^n", "ⁿ")
        .replace("_0", "₀")
        .replace("_1", "₁")
        .replace("_2", "₂")
        .replace("_n", "ₙ")
        .replace("\\text{", "")
        .replace("\\mathbf{", "")
}
