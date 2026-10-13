package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.db.PromptTemplateEntity
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CardSurfaceLight
import com.example.ui.theme.HeadlineDark
import com.example.ui.theme.HeadlineLight
import com.example.ui.theme.SalimCard
import com.example.ui.theme.SalimPrimaryButton
import com.example.ui.theme.SalimSecondaryButton
import com.example.ui.theme.SalimShape
import com.example.ui.theme.SalimTextStyles
import com.example.ui.theme.SecondaryCaptionDark
import com.example.ui.theme.SecondaryCaptionLight
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesSheet(
    viewModel: MainViewModel,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val templates by viewModel.templates.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newShortcut by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }

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
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Prompt Templates",
                        style = SalimTextStyles.HeadlineMedium,
                        color = headlineColor
                    )
                    Text(
                        text = "Reusable shortcuts and structured prompts",
                        style = SalimTextStyles.Caption,
                        color = captionColor
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = captionColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Template Button
            SalimSecondaryButton(
                text = "New Prompt Template",
                onClick = { showCreateDialog = true },
                modifier = Modifier.fillMaxWidth(),
                isDark = isDark,
                leadingIcon = {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(templates, key = { it.id }) { template ->
                    SalimCard(
                        isDark = isDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = template.title,
                                        style = SalimTextStyles.Body.copy(fontWeight = FontWeight.SemiBold),
                                        color = headlineColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(SalimShape)
                                            .background(AccentGreen.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = template.shortcut,
                                            style = SalimTextStyles.Caption,
                                            color = AccentGreen
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.deleteTemplate(template.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete template",
                                        tint = captionColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = template.content,
                                style = SalimTextStyles.Secondary,
                                color = captionColor,
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            SalimPrimaryButton(
                                text = "Use Template",
                                onClick = {
                                    viewModel.insertTemplateText(template.content)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                isDark = isDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text("Create Prompt Template", style = SalimTextStyles.Title, color = headlineColor)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Title", style = SalimTextStyles.Caption, color = captionColor)
                    BasicTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SalimShape)
                            .background(if (isDark) Color(0xFF141715) else Color(0xFFF2F5EF))
                            .padding(12.dp),
                        textStyle = SalimTextStyles.Body.copy(color = headlineColor),
                        singleLine = true
                    )

                    Text("Shortcut (e.g. /summary)", style = SalimTextStyles.Caption, color = captionColor)
                    BasicTextField(
                        value = newShortcut,
                        onValueChange = { newShortcut = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SalimShape)
                            .background(if (isDark) Color(0xFF141715) else Color(0xFFF2F5EF))
                            .padding(12.dp),
                        textStyle = SalimTextStyles.Body.copy(color = headlineColor),
                        singleLine = true
                    )

                    Text("Prompt Content", style = SalimTextStyles.Caption, color = captionColor)
                    BasicTextField(
                        value = newContent,
                        onValueChange = { newContent = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(SalimShape)
                            .background(if (isDark) Color(0xFF141715) else Color(0xFFF2F5EF))
                            .padding(12.dp),
                        textStyle = SalimTextStyles.Body.copy(color = headlineColor)
                    )
                }
            },
            confirmButton = {
                SalimPrimaryButton(
                    text = "Save Template",
                    onClick = {
                        if (newTitle.isNotBlank() && newContent.isNotBlank()) {
                            val shortcutFormatted = if (newShortcut.startsWith("/")) newShortcut else "/$newShortcut"
                            viewModel.createTemplate(newTitle.trim(), shortcutFormatted.trim(), newContent.trim())
                            newTitle = ""
                            newShortcut = ""
                            newContent = ""
                            showCreateDialog = false
                        }
                    },
                    isDark = isDark
                )
            },
            dismissButton = {
                SalimSecondaryButton(
                    text = "Cancel",
                    onClick = { showCreateDialog = false },
                    isDark = isDark
                )
            },
            containerColor = sheetBg,
            shape = SalimShape
        )
    }
}
