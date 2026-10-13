package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.AvailableModels
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CardSurfaceLight
import com.example.ui.theme.HeadlineDark
import com.example.ui.theme.HeadlineLight
import com.example.ui.theme.SalimCard
import com.example.ui.theme.SalimCornerRadius
import com.example.ui.theme.SalimPill
import com.example.ui.theme.SalimPrimaryButton
import com.example.ui.theme.SalimSecondaryButton
import com.example.ui.theme.SalimShape
import com.example.ui.theme.SalimTextStyles
import com.example.ui.theme.SecondaryCaptionDark
import com.example.ui.theme.SecondaryCaptionLight
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    viewModel: MainViewModel,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val preferences = viewModel.preferences

    val currentTheme by preferences.themeMode.collectAsState()
    val currentModel by preferences.selectedModel.collectAsState()
    val responseStyle by preferences.responseStyle.collectAsState()
    val responseLength by preferences.responseLength.collectAsState()
    val bubbleDensity by preferences.bubbleDensity.collectAsState()
    val fontScaleOverride by preferences.fontScaleOverride.collectAsState()
    val contextTrimming by preferences.contextTrimmingStrategy.collectAsState()
    val globalInstructions by preferences.globalInstructions.collectAsState()
    val temperature by preferences.temperature.collectAsState()
    val topP by preferences.topP.collectAsState()
    val maxTokens by preferences.maxTokens.collectAsState()
    val showTokenUsage by preferences.showTokenUsage.collectAsState()
    val enterToSend by preferences.enterToSend.collectAsState()
    val customApiKey by preferences.customApiKey.collectAsState()
    val cloudBackup by preferences.cloudBackupEnabled.collectAsState()

    var editingInstructions by remember(globalInstructions) { mutableStateOf(globalInstructions) }
    var editingApiKey by remember(customApiKey) { mutableStateOf(customApiKey) }
    var apiKeyVisible by remember { mutableStateOf(false) }
    var showAdvanced by remember { mutableStateOf(false) }

    val headlineColor = if (isDark) HeadlineDark else HeadlineLight
    val captionColor = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight
    val sheetBg = if (isDark) CardSurfaceDark else CardSurfaceLight

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        shape = SalimShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Preferences",
                    style = SalimTextStyles.HeadlineMedium,
                    color = headlineColor
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = captionColor)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Appearance / Theme
            Text("Appearance", style = SalimTextStyles.Title, color = headlineColor)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionButton(
                    label = "System",
                    isSelected = currentTheme == "system",
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    onClick = { preferences.setThemeMode("system") }
                )
                ThemeOptionButton(
                    label = "Light",
                    isSelected = currentTheme == "light",
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    onClick = { preferences.setThemeMode("light") }
                )
                ThemeOptionButton(
                    label = "Dark",
                    isSelected = currentTheme == "dark",
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    onClick = { preferences.setThemeMode("dark") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Response Style Preset
            Text("Response Style", style = SalimTextStyles.Title, color = headlineColor)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Governs response depth, brevity, and generation parameters.",
                style = SalimTextStyles.Caption,
                color = captionColor
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ResponseStyleCard(
                    title = "Concise",
                    description = "Direct, crisp answers without conversational preamble (Temp: 0.4)",
                    isSelected = responseStyle == "concise",
                    isDark = isDark,
                    onClick = { preferences.setResponseStyle("concise") }
                )
                ResponseStyleCard(
                    title = "Balanced",
                    description = "Natural cadence with thoughtful, structured depth (Temp: 0.7)",
                    isSelected = responseStyle == "balanced",
                    isDark = isDark,
                    onClick = { preferences.setResponseStyle("balanced") }
                )
                ResponseStyleCard(
                    title = "Detailed",
                    description = "Exhaustive analysis, rich nuance, and complete elaboration (Temp: 0.85)",
                    isSelected = responseStyle == "detailed",
                    isDark = isDark,
                    onClick = { preferences.setResponseStyle("detailed") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Response Length / Effort Pills
            Text("Response Depth & Effort", style = SalimTextStyles.Title, color = headlineColor)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Target length and thoroughness across dialogue exchanges.",
                style = SalimTextStyles.Caption,
                color = captionColor
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SalimPill(
                    text = "Concise",
                    isSelected = responseLength == "short",
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    onClick = { preferences.setResponseLength("short") }
                )
                SalimPill(
                    text = "Balanced",
                    isSelected = responseLength == "medium",
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    onClick = { preferences.setResponseLength("medium") }
                )
                SalimPill(
                    text = "Deep",
                    isSelected = responseLength == "long",
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    onClick = { preferences.setResponseLength("long") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Message Spacing Density Pills
            Text("Message Spacing Density", style = SalimTextStyles.Title, color = headlineColor)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Compact spacing for speed or comfortable for readability.",
                style = SalimTextStyles.Caption,
                color = captionColor
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SalimPill(
                    text = "Compact",
                    isSelected = bubbleDensity == "compact",
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    onClick = { preferences.setBubbleDensity("compact") }
                )
                SalimPill(
                    text = "Comfortable",
                    isSelected = bubbleDensity == "comfortable",
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    onClick = { preferences.setBubbleDensity("comfortable") }
                )
                SalimPill(
                    text = "Spacious",
                    isSelected = bubbleDensity == "spacious",
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    onClick = { preferences.setBubbleDensity("spacious") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Typography Size Pills
            Text("Typography Size", style = SalimTextStyles.Title, color = headlineColor)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Adjust text scale for all dialogues and markdown components.",
                style = SalimTextStyles.Caption,
                color = captionColor
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SalimPill(
                    text = "Default 100%",
                    isSelected = fontScaleOverride <= 1.05f && fontScaleOverride >= 0.95f,
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    onClick = { preferences.setFontScaleOverride(1.0f) }
                )
                SalimPill(
                    text = "Large 115%",
                    isSelected = fontScaleOverride > 1.05f && fontScaleOverride <= 1.2f,
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    onClick = { preferences.setFontScaleOverride(1.15f) }
                )
                SalimPill(
                    text = "X-Large 130%",
                    isSelected = fontScaleOverride > 1.2f,
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    onClick = { preferences.setFontScaleOverride(1.3f) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Global Custom Instructions
            Text("Global Instructions", style = SalimTextStyles.Title, color = headlineColor)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Applied to all conversations unless overridden locally.",
                style = SalimTextStyles.Caption,
                color = captionColor
            )
            Spacer(modifier = Modifier.height(10.dp))
            BasicTextField(
                value = editingInstructions,
                onValueChange = {
                    editingInstructions = it
                    preferences.setGlobalInstructions(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(SalimShape)
                    .background(if (isDark) Color(0xFF141715) else Color(0xFFF2F5EF))
                    .padding(14.dp),
                textStyle = SalimTextStyles.Body.copy(color = headlineColor),
                decorationBox = { inner ->
                    if (editingInstructions.isEmpty()) {
                        Text(
                            "e.g. Always format formulas in LaTeX, speak candidly, favor clean Kotlin code...",
                            style = SalimTextStyles.Body,
                            color = captionColor
                        )
                    }
                    inner()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Quick Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Enter to Send", style = SalimTextStyles.Body, color = headlineColor)
                    Text("Pressing enter sends the message immediately", style = SalimTextStyles.Caption, color = captionColor)
                }
                Switch(
                    checked = enterToSend,
                    onCheckedChange = { preferences.setEnterToSend(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = if (isDark) AccentGold else AccentGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Cloud Sync & Backup", style = SalimTextStyles.Body, color = headlineColor)
                    Text("Off by default. Keep all dialogues strictly local.", style = SalimTextStyles.Caption, color = captionColor)
                }
                Switch(
                    checked = cloudBackup,
                    onCheckedChange = { preferences.setCloudBackupEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = if (isDark) AccentGold else AccentGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Prompt Templates shortcut button
            SalimSecondaryButton(
                text = "Manage Prompt Templates",
                onClick = {
                    onDismiss()
                    viewModel.isTemplatesSheetOpen.value = true
                },
                modifier = Modifier.fillMaxWidth(),
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 6. Transparent Provider Disclosure (Feature 36)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SalimShape)
                    .background(if (isDark) Color(0xFF141715) else Color(0xFFF2F5EF))
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = if (isDark) AccentGold else AccentGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Data & Inference Disclosure",
                        style = SalimTextStyles.Body.copy(fontWeight = FontWeight.SemiBold),
                        color = headlineColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Salim processes prompts through a high-speed cloud inference network. All dialogue history, tags, and drafts remain strictly on your local device. No telemetry, ads, or third-party trackers are included.",
                        style = SalimTextStyles.Caption,
                        color = captionColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 7. Advanced & Inference Settings (Clearly separated)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SalimShape)
                    .clickable { showAdvanced = !showAdvanced }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = headlineColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Advanced & Inference Engine", style = SalimTextStyles.Title, color = headlineColor)
                }
                Icon(
                    imageVector = if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = captionColor
                )
            }

            AnimatedVisibility(visible = showAdvanced) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    // Model Selection
                    Text("Intelligence Engine", style = SalimTextStyles.Body.copy(fontWeight = FontWeight.Bold), color = headlineColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    AvailableModels.forEach { model ->
                        ModelSelectionCard(
                            model = model,
                            isSelected = currentModel == model.id,
                            isDark = isDark,
                            onClick = { preferences.setSelectedModel(model.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // API Key
                    Text("Custom Connection Key (Optional)", style = SalimTextStyles.Body, color = headlineColor)
                    Text("Uses Secrets panel GROQ_API_KEY by default", style = SalimTextStyles.Caption, color = captionColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SalimShape)
                            .background(if (isDark) Color(0xFF141715) else Color(0xFFF2F5EF))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = editingApiKey,
                            onValueChange = {
                                editingApiKey = it
                                preferences.setCustomApiKey(it)
                            },
                            visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.weight(1f),
                            textStyle = SalimTextStyles.Body.copy(color = headlineColor),
                            singleLine = true,
                            decorationBox = { inner ->
                                if (editingApiKey.isEmpty()) {
                                    Text("gsk_...", style = SalimTextStyles.Body, color = captionColor)
                                }
                                inner()
                            }
                        )
                        IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                            Icon(
                                imageVector = if (apiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle visibility",
                                tint = captionColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Temperature slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Temperature", style = SalimTextStyles.Body, color = headlineColor)
                        Text(String.format("%.2f", temperature), style = SalimTextStyles.Caption, color = captionColor)
                    }
                    Slider(
                        value = temperature,
                        onValueChange = { preferences.setTemperature(it) },
                        valueRange = 0.0f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = if (isDark) AccentGold else AccentGreen,
                            activeTrackColor = if (isDark) AccentGold else AccentGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Top-P slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Top-P Nucleus", style = SalimTextStyles.Body, color = headlineColor)
                        Text(String.format("%.2f", topP), style = SalimTextStyles.Caption, color = captionColor)
                    }
                    Slider(
                        value = topP,
                        onValueChange = { preferences.setTopP(it) },
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = if (isDark) AccentGold else AccentGreen,
                            activeTrackColor = if (isDark) AccentGold else AccentGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Max Tokens slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Max Output Tokens", style = SalimTextStyles.Body, color = headlineColor)
                        Text("$maxTokens tokens", style = SalimTextStyles.Caption, color = captionColor)
                    }
                    Slider(
                        value = maxTokens.toFloat(),
                        onValueChange = { preferences.setMaxTokens(it.toInt()) },
                        valueRange = 256f..8192f,
                        colors = SliderDefaults.colors(
                            thumbColor = if (isDark) AccentGold else AccentGreen,
                            activeTrackColor = if (isDark) AccentGold else AccentGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Token Usage Counter Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Token Usage Counter", style = SalimTextStyles.Body, color = headlineColor)
                            Text("Display completion token count below responses", style = SalimTextStyles.Caption, color = captionColor)
                        }
                        Switch(
                            checked = showTokenUsage,
                            onCheckedChange = { preferences.setShowTokenUsage(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = if (isDark) AccentGold else AccentGreen
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Context Window Strategy", style = SalimTextStyles.Body.copy(fontWeight = FontWeight.Bold), color = headlineColor)
                    Text("Dialogue memory behavior as conversations approach token limits", style = SalimTextStyles.Caption, color = captionColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SalimPill(
                            text = "Warn",
                            isSelected = contextTrimming == "warn",
                            isDark = isDark,
                            modifier = Modifier.weight(1f),
                            onClick = { preferences.setContextTrimmingStrategy("warn") }
                        )
                        SalimPill(
                            text = "Auto-Trim",
                            isSelected = contextTrimming == "auto_trim",
                            isDark = isDark,
                            modifier = Modifier.weight(1f),
                            onClick = { preferences.setContextTrimmingStrategy("auto_trim") }
                        )
                        SalimPill(
                            text = "Keep All",
                            isSelected = contextTrimming == "keep_all",
                            isDark = isDark,
                            modifier = Modifier.weight(1f),
                            onClick = { preferences.setContextTrimmingStrategy("keep_all") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 8. Delete All Data
            SalimSecondaryButton(
                text = "Clear All Local Data",
                onClick = { viewModel.showDeleteAllDataDialog.value = true },
                modifier = Modifier.fillMaxWidth(),
                isDark = isDark,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Delete All Data Confirmation Dialog
    val showDeleteDialog by viewModel.showDeleteAllDataDialog.collectAsState()
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showDeleteAllDataDialog.value = false },
            title = {
                Text("Erase All Data?", style = SalimTextStyles.Title, color = headlineColor)
            },
            text = {
                Text(
                    text = "This action will permanently delete all conversations, saved messages, templates, and customized preferences. This action cannot be reversed.",
                    style = SalimTextStyles.Body,
                    color = captionColor
                )
            },
            confirmButton = {
                SalimPrimaryButton(
                    text = "Erase Everything",
                    onClick = {
                        viewModel.deleteAllDataConfirmed()
                        onDismiss()
                    },
                    isDark = isDark
                )
            },
            dismissButton = {
                SalimSecondaryButton(
                    text = "Cancel",
                    onClick = { viewModel.showDeleteAllDataDialog.value = false },
                    isDark = isDark
                )
            },
            containerColor = sheetBg,
            shape = SalimShape
        )
    }
}

@Composable
private fun ThemeOptionButton(
    label: String,
    isSelected: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) {
        if (isDark) Color(0xFFE8EAE4) else Color(0xFF191C1A)
    } else {
        if (isDark) Color(0xFF141715) else Color(0xFFF2F5EF)
    }

    val textColor = if (isSelected) {
        if (isDark) Color(0xFF141715) else Color.White
    } else {
        if (isDark) HeadlineDark else HeadlineLight
    }

    val borderColor = if (isSelected) {
        if (isDark) AccentGold else AccentGreen
    } else {
        if (isDark) Color(0xFF2E332E) else Color(0xFFDDE2D8)
    }

    Box(
        modifier = modifier
            .clip(SalimShape)
            .border(width = if (isSelected) 1.5.dp else 1.dp, color = borderColor, shape = SalimShape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = SalimTextStyles.Secondary.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = textColor
            )
        }
    }
}

@Composable
private fun ResponseStyleCard(
    title: String,
    description: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        if (isDark) AccentGold else AccentGreen
    } else {
        if (isDark) Color(0xFF2E332E) else Color(0xFFE2E6DF)
    }

    val cardBg = if (isSelected) {
        if (isDark) Color(0xFF1A221A) else Color(0xFFF0F5EC)
    } else {
        if (isDark) Color(0xFF141715) else Color(0xFFFAFBF8)
    }

    val titleColor = if (isSelected) {
        if (isDark) AccentGold else AccentGreen
    } else {
        if (isDark) HeadlineDark else HeadlineLight
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SalimShape)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, SalimShape)
            .background(cardBg)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = SalimTextStyles.Body.copy(fontWeight = FontWeight.Bold),
                color = titleColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = SalimTextStyles.Caption,
                color = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isDark) AccentGold else AccentGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF141715) else Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ModelSelectionCard(
    model: com.example.data.preferences.SalimModelInfo,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        if (isDark) AccentGold else AccentGreen
    } else {
        if (isDark) Color(0xFF2E332E) else Color(0xFFE2E6DF)
    }

    val cardBg = if (isSelected) {
        if (isDark) Color(0xFF1A221A) else Color(0xFFF0F5EC)
    } else {
        if (isDark) Color(0xFF141715) else Color(0xFFFAFBF8)
    }

    val titleColor = if (isSelected) {
        if (isDark) AccentGold else AccentGreen
    } else {
        if (isDark) HeadlineDark else HeadlineLight
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SalimShape)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, SalimShape)
            .background(cardBg)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = model.displayName,
                    style = SalimTextStyles.Body.copy(fontWeight = FontWeight.Bold),
                    color = titleColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(SalimShape)
                        .background(if (isSelected) (if (isDark) AccentGold else AccentGreen) else AccentGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = model.badge,
                        style = SalimTextStyles.Caption.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) (if (isDark) Color(0xFF141715) else Color.White) else AccentGreen
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = model.description,
                style = SalimTextStyles.Caption,
                color = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = model.id,
                style = SalimTextStyles.Caption.copy(fontSize = 11.sp),
                color = (if (isDark) SecondaryCaptionDark else SecondaryCaptionLight).copy(alpha = 0.6f)
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isDark) AccentGold else AccentGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF141715) else Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
