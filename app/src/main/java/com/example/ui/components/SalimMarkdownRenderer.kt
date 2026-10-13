package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.SalimCodeFontFamily
import com.example.ui.theme.SalimCornerRadius
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
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock()
    object Divider : MarkdownBlock()
}

/**
 * SalimMarkdownRenderer
 *
 * Implements Section M:
 * 1. Two-typeface system: Serif display face (Lora) for headings H1/H2/H3, sans-serif (Plus Jakarta Sans) for body.
 * 2. Bold-term-then-explanation list pattern for bulleted and numbered lists.
 * 3. Zero leaked syntax with mid-stream incomplete formatting buffer.
 * 4. Generous vertical whitespace between paragraphs (14dp), list items (8dp), and headings (20dp top).
 * 5. Word-level highlighting during text-to-speech read aloud (Section U.18).
 * 6. Selectable text for "Explain this" contextual comprehension (Section R.9).
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
            verticalArrangement = Arrangement.spacedBy(14.dp) // Generous paragraph whitespace
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
                        RenderCodeBlock(
                            language = block.language,
                            code = block.code,
                            isDark = isDark,
                            fontScale = fontScale
                        )
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
                            block.items.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "•",
                                        style = SalimTextStyles.Body.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = (17 * fontScale).sp
                                        ),
                                        color = if (isDark) AccentGold else AccentGreen,
                                        modifier = Modifier.padding(start = 2.dp, end = 10.dp)
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
                            isDark = isDark,
                            fontScale = fontScale
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
 * Bold-term-then-explanation list pattern (Section M.3):
 * In numbered or bulleted lists, the lead term or phrase of each item renders in bold,
 * followed by an em-dash or colon, continuing in regular weight for the explanation.
 */
private fun formatBoldTermListInline(
    itemText: String,
    baseColor: Color,
    isDark: Boolean,
    activeWordRange: Pair<Int, Int>?,
    fullContent: String,
    isStreaming: Boolean = false
): AnnotatedString {
    // Case 1: Item already has explicit bold at the beginning: "**Term**: rest" or "**Term** — rest" or "**Term** rest"
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
    // If text ends with incomplete bold/italic/code markers, strip them cleanly
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

@Composable
private fun RenderTable(
    headers: List<String>,
    rows: List<List<String>>,
    isDark: Boolean,
    fontScale: Float
) {
    val tableBg = if (isDark) Color(0xFF1B1E1B) else Color(0xFFF4F6F2)
    val borderColor = if (isDark) Color(0xFF2C322C) else Color(0xFFDDE2DA)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SalimShape)
            .background(tableBg)
            .horizontalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Row(modifier = Modifier.padding(vertical = 4.dp)) {
            headers.forEach { header ->
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = header.trim(),
                        style = SalimTextStyles.Secondary.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = (15 * fontScale).sp
                        ),
                        color = if (isDark) Color(0xFFE5EAE2) else Color(0xFF1E2420)
                    )
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = borderColor)

        rows.forEach { row ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                row.forEachIndexed { i, cell ->
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = cell.trim(),
                            style = SalimTextStyles.Secondary.copy(fontSize = (15 * fontScale).sp),
                            color = if (isDark) Color(0xFFC8CEC5) else Color(0xFF333B35)
                        )
                    }
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = borderColor.copy(alpha = 0.5f))
        }
    }
}

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

        // Bullet lists
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

        // Table
        if (line.contains("|") && i + 1 < lines.size && lines[i + 1].contains("---")) {
            val headers = line.split("|").filter { it.isNotBlank() }
            i += 2
            val rows = mutableListOf<List<String>>()
            while (i < lines.size && lines[i].contains("|")) {
                val rowCells = lines[i].split("|").filter { it.isNotBlank() }
                if (rowCells.isNotEmpty()) rows.add(rowCells)
                i++
            }
            blocks.add(MarkdownBlock.Table(headers, rows))
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

/**
 * Builds annotated string for inline tokens: bold, italic, code, math, strikethrough.
 * Also applies real-time word-level TTS highlighting if activeWordRange is provided.
 */
private fun buildAnnotatedInline(
    text: String,
    baseColor: Color,
    isDark: Boolean,
    activeWordRange: Pair<Int, Int>? = null,
    fullContent: String = "",
    isStreaming: Boolean = false
): AnnotatedString {
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
                    // Mid-stream incomplete code block: render code style without leaking raw backtick
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
                    // Mid-stream incomplete link: render text without raw '['
                    val partialLabel = text.substring(cursor + 1)
                    append(partialLabel)
                    break
                }
            }

            // Bold: **bold** or __bold__
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
                    // Mid-stream incomplete bold: render remaining text bold without raw asterisks
                    val boldSpan = text.substring(cursor + 2)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor)) {
                        append(boldSpan)
                    }
                    break
                }
            }

            // Strikethrough: ~~strikethrough~~
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

            // Italic: *italic* or _italic_
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
