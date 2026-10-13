package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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

private sealed class MarkdownBlock {
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

@Composable
fun SalimMarkdownRenderer(
    content: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    isDark: Boolean = false
) {
    val blocks = remember(content) { parseMarkdownBlocks(content) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    val style = when (block.level) {
                        1 -> SalimTextStyles.HeadlineMedium
                        2 -> SalimTextStyles.Title
                        else -> SalimTextStyles.Body.copy(fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = buildAnnotatedInline(block.text, textColor, isDark),
                        style = style,
                        color = textColor
                    )
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = buildAnnotatedInline(block.text, textColor, isDark),
                        style = SalimTextStyles.Body,
                        color = textColor
                    )
                }
                is MarkdownBlock.CodeBlock -> {
                    RenderCodeBlock(
                        language = block.language,
                        code = block.code,
                        isDark = isDark
                    )
                }
                is MarkdownBlock.MathBlock -> {
                    RenderMathBlock(
                        latex = block.latex,
                        isDark = isDark
                    )
                }
                is MarkdownBlock.BlockQuote -> {
                    RenderBlockQuote(
                        text = block.text,
                        textColor = textColor,
                        isDark = isDark
                    )
                }
                is MarkdownBlock.BulletList -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        block.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "• ",
                                    style = SalimTextStyles.Body.copy(fontWeight = FontWeight.Bold),
                                    color = if (isDark) AccentGold else AccentGreen,
                                    modifier = Modifier.padding(start = 4.dp, end = 6.dp)
                                )
                                Text(
                                    text = buildAnnotatedInline(item, textColor, isDark),
                                    style = SalimTextStyles.Body,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
                is MarkdownBlock.NumberedList -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        block.items.forEachIndexed { idx, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${idx + 1}. ",
                                    style = SalimTextStyles.Body.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isDark) AccentGold else AccentGreen,
                                    modifier = Modifier.padding(start = 4.dp, end = 6.dp)
                                )
                                Text(
                                    text = buildAnnotatedInline(item, textColor, isDark),
                                    style = SalimTextStyles.Body,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
                is MarkdownBlock.Table -> {
                    RenderTable(
                        headers = block.headers,
                        rows = block.rows,
                        isDark = isDark
                    )
                }
                is MarkdownBlock.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 1.dp,
                        color = if (isDark) Color(0xFF333833) else Color(0xFFD6DAD4)
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderCodeBlock(
    language: String,
    code: String,
    isDark: Boolean
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
        // Code header
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

        // Horizontal scrollable code body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(14.dp)
        ) {
            Text(
                text = code,
                style = SalimTextStyles.Code,
                color = Color(0xFFF1F5F0)
            )
        }
    }
}

@Composable
private fun RenderMathBlock(
    latex: String,
    isDark: Boolean
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
                letterSpacing = 0.5.sp
            ),
            color = if (isDark) Color(0xFFD6DBD3) else Color(0xFF263028)
        )
    }
}

@Composable
private fun RenderBlockQuote(
    text: String,
    textColor: Color,
    isDark: Boolean
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
                .height(28.dp)
                .clip(CircleShape)
                .background(barColor)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = buildAnnotatedInline(text, textColor, isDark),
            style = SalimTextStyles.Body.copy(fontStyle = FontStyle.Italic),
            color = textColor.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun RenderTable(
    headers: List<String>,
    rows: List<List<String>>,
    isDark: Boolean
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
        // Headers
        Row(modifier = Modifier.padding(vertical = 4.dp)) {
            headers.forEach { header ->
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = header.trim(),
                        style = SalimTextStyles.Secondary.copy(fontWeight = FontWeight.Bold),
                        color = if (isDark) Color(0xFFE5EAE2) else Color(0xFF1E2420)
                    )
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = borderColor)

        // Rows
        rows.forEach { row ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                row.forEachIndexed { i, cell ->
                    Box(
                        modifier = Modifier
                            .width(130.dp)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = cell.trim(),
                            style = SalimTextStyles.Secondary,
                            color = if (isDark) Color(0xFFC8CEC5) else Color(0xFF333B35)
                        )
                    }
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = borderColor.copy(alpha = 0.5f))
        }
    }
}

/**
 * Parses markdown into structured blocks while streaming gracefully.
 */
private fun parseMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = rawText.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Check fenced code block
        if (line.trimStart().startsWith("```")) {
            val language = line.trimStart().removePrefix("```").trim()
            val codeBuilder = StringBuilder()
            i++
            var closed = false
            while (i < lines.size) {
                if (lines[i].trimStart().startsWith("```")) {
                    closed = true
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

        // Check block math: $$ ... $$
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

        // Headings
        if (line.startsWith("# ")) {
            blocks.add(MarkdownBlock.Heading(1, line.removePrefix("# ").trim()))
            i++
            continue
        }
        if (line.startsWith("## ")) {
            blocks.add(MarkdownBlock.Heading(2, line.removePrefix("## ").trim()))
            i++
            continue
        }
        if (line.startsWith("### ")) {
            blocks.add(MarkdownBlock.Heading(3, line.removePrefix("### ").trim()))
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
            i += 2 // skip header & separator
            val rows = mutableListOf<List<String>>()
            while (i < lines.size && lines[i].contains("|")) {
                val rowCells = lines[i].split("|").filter { it.isNotBlank() }
                if (rowCells.isNotEmpty()) rows.add(rowCells)
                i++
            }
            blocks.add(MarkdownBlock.Table(headers, rows))
            continue
        }

        // Regular paragraph or empty line
        if (line.isBlank()) {
            i++
            continue
        }

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
 * Parses inline spans: bold, italic, code, math.
 */
private fun buildAnnotatedInline(
    text: String,
    baseColor: Color,
    isDark: Boolean
): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val len = text.length

        while (cursor < len) {
            // Check inline code: `code`
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
                }
            }

            // Check inline math: $formula$
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

            // Check bold: **bold**
            if (cursor + 1 < len && text.substring(cursor, cursor + 2) == "**") {
                val end = text.indexOf("**", cursor + 2)
                if (end != -1) {
                    val boldSpan = text.substring(cursor + 2, end)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor)) {
                        append(boldSpan)
                    }
                    cursor = end + 2
                    continue
                }
            }

            // Check italic: *italic*
            if (text[cursor] == '*' && (cursor + 1 < len && text[cursor + 1] != '*')) {
                val end = text.indexOf('*', cursor + 1)
                if (end != -1) {
                    val italicSpan = text.substring(cursor + 1, end)
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = baseColor)) {
                        append(italicSpan)
                    }
                    cursor = end + 1
                    continue
                }
            }

            // Regular character
            append(text[cursor])
            cursor++
        }
    }
}

/**
 * Transforms common LaTeX math syntax into clean readable mathematical expressions.
 */
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
