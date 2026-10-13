package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CardSurfaceLight
import com.example.ui.theme.HeadlineDark
import com.example.ui.theme.HeadlineLight
import com.example.ui.theme.SalimCard
import com.example.ui.theme.SalimCodeFontFamily
import com.example.ui.theme.SalimCornerRadius
import com.example.ui.theme.SalimPrimaryButton
import com.example.ui.theme.SalimSecondaryButton
import com.example.ui.theme.SalimShape
import com.example.ui.theme.SalimTextStyles
import com.example.ui.theme.SecondaryCaptionDark
import com.example.ui.theme.SecondaryCaptionLight

data class ArtifactItem(
    val id: String,
    val title: String,
    val type: String, // "code", "html", "markdown", "document"
    val language: String,
    val content: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtifactPanelSheet(
    artifact: ArtifactItem,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSendBackAsContext: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var editableContent by remember(artifact) { mutableStateOf(artifact.content) }
    var isCopied by remember { mutableStateOf(false) }
    val headlineColor = if (isDark) HeadlineDark else HeadlineLight
    val captionColor = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight
    val sheetBg = if (isDark) CardSurfaceDark else CardSurfaceLight

    val isPreviewable = artifact.language.equals("html", true) ||
            artifact.language.equals("svg", true) ||
            artifact.language.equals("markdown", true) ||
            artifact.language.equals("md", true) ||
            artifact.language.equals("mermaid", true) ||
            artifact.language.equals("chart", true)

    val availableTabs = remember(isPreviewable) {
        if (isPreviewable) {
            listOf("Safe Preview", "Live Editor")
        } else {
            listOf("Live Editor")
        }
    }
    var selectedTabIndex by remember(artifact.id) {
        mutableIntStateOf(0)
    }
    val safeTabIndex = selectedTabIndex.coerceIn(0, availableTabs.lastIndex.coerceAtLeast(0))
    val currentTab = availableTabs.getOrElse(safeTabIndex) { "Live Editor" }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        shape = SalimShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(SalimShape)
                            .background(AccentGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = artifact.title,
                            style = SalimTextStyles.Title,
                            color = headlineColor
                        )
                        Text(
                            text = "${artifact.language.uppercase()} Artifact",
                            style = SalimTextStyles.Caption,
                            color = captionColor
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText(artifact.title, editableContent))
                            isCopied = true
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            scope.launch {
                                delay(2000)
                                isCopied = false
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = if (isCopied) "Copied" else "Copy Code",
                            tint = if (isCopied) AccentGreen else captionColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, editableContent)
                                putExtra(Intent.EXTRA_TITLE, artifact.title)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export ${artifact.title}"))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export Artifact", tint = captionColor, modifier = Modifier.size(18.dp))
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = captionColor, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab bar: Live Editor vs Sandbox Preview
            TabRow(
                selectedTabIndex = safeTabIndex,
                containerColor = Color.Transparent,
                contentColor = headlineColor,
                indicator = { tabPositions ->
                    if (safeTabIndex in tabPositions.indices) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[safeTabIndex]),
                            color = if (isDark) AccentGold else AccentGreen
                        )
                    }
                }
            ) {
                availableTabs.forEachIndexed { index, title ->
                    val isSelected = safeTabIndex == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = SalimTextStyles.Secondary.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) (if (isDark) AccentGold else AccentGreen) else captionColor
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (currentTab == "Safe Preview" && isPreviewable) {
                    val isMarkdownOrChart = artifact.language.equals("markdown", true) ||
                            artifact.language.equals("md", true) ||
                            artifact.language.equals("mermaid", true) ||
                            artifact.language.equals("chart", true)

                    if (isMarkdownOrChart) {
                        val previewContent = remember(editableContent, artifact.language) {
                            if (artifact.language.equals("mermaid", true) || artifact.language.equals("chart", true)) {
                                "```${artifact.language}\n$editableContent\n```"
                            } else {
                                editableContent
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(SalimShape)
                                .background(if (isDark) Color(0xFF141715) else Color(0xFFF9FAF8))
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            SalimMarkdownRenderer(
                                content = previewContent,
                                textColor = headlineColor,
                                isDark = isDark
                            )
                        }
                    } else {
                        // Safe Sandboxed WebView preview for HTML/SVG
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(SalimShape)
                                .background(Color.White)
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        settings.allowFileAccess = false
                                        settings.allowContentAccess = false
                                        webViewClient = object : WebViewClient() {}
                                        loadDataWithBaseURL(null, editableContent, "text/html", "UTF-8", null)
                                    }
                                },
                                update = { webView ->
                                    webView.loadDataWithBaseURL(null, editableContent, "text/html", "UTF-8", null)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                } else {
                    // Live Editor
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(SalimShape)
                            .background(if (isDark) Color(0xFF141715) else Color(0xFFF1F4EE))
                            .padding(14.dp)
                    ) {
                        BasicTextField(
                            value = editableContent,
                            onValueChange = { editableContent = it },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = SalimTextStyles.Code.copy(
                                color = if (isDark) Color(0xFFE4E9E2) else Color(0xFF1C221D)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SalimSecondaryButton(
                    text = "Download Native",
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, editableContent)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Save Native ${artifact.title}"))
                    },
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )

                SalimPrimaryButton(
                    text = "Send as Context",
                    onClick = {
                        onSendBackAsContext("Here is the updated ${artifact.title}:\n\n```${artifact.language}\n$editableContent\n```")
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    isDark = isDark,
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
