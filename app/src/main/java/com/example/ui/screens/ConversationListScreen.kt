package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.db.ConversationEntity
import com.example.data.db.FolderEntity
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CardSurfaceLight
import com.example.ui.theme.HeadlineDark
import com.example.ui.theme.HeadlineLight
import com.example.ui.theme.SalimCard
import com.example.ui.theme.SalimCornerRadius
import com.example.ui.theme.SalimGradientBackground
import com.example.ui.theme.SalimPrimaryButton
import com.example.ui.theme.SalimSecondaryButton
import com.example.ui.theme.SalimShape
import com.example.ui.theme.SalimTextStyles
import com.example.ui.theme.SecondaryCaptionDark
import com.example.ui.theme.SecondaryCaptionLight
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConversationListScreen(
    viewModel: MainViewModel,
    isDark: Boolean
) {
    val conversations by viewModel.conversationsList.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val selectedFolderId by viewModel.selectedFolderId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showArchived by viewModel.showArchived.collectAsState()

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    val headlineColor = if (isDark) HeadlineDark else HeadlineLight
    val captionColor = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight

    SalimGradientBackground(isDark = isDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Minimal Top Navigation sitting directly on the gradient
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (showArchived) "Archived" else "Salim",
                        style = SalimTextStyles.HeadlineLarge,
                        color = headlineColor
                    )
                    Text(
                        text = if (showArchived) "Preserved dialogues" else "Conversational intelligence",
                        style = SalimTextStyles.Caption,
                        color = captionColor
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Starred messages view button
                    IconButton(
                        onClick = { viewModel.navigateToStarred() },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("starred_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Starred messages",
                            tint = if (isDark) AccentGold else Color(0xFF5E655C)
                        )
                    }

                    // Settings button
                    IconButton(
                        onClick = { viewModel.isSettingsSheetOpen.value = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (isDark) HeadlineDark else Color(0xFF5E655C)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Persistent New Chat Control
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(SalimShape)
                            .background(if (isDark) Color(0xFFE8EAE4) else Color(0xFF191C1A))
                            .clickable { viewModel.navigateToNewChat() }
                            .testTag("new_chat_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Start new conversation",
                            tint = if (isDark) Color(0xFF141715) else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Search Bar: fully opaque white card with 22dp radius
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                SalimCard(
                    isDark = isDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = captionColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_input"),
                            textStyle = SalimTextStyles.Body.copy(color = headlineColor),
                            cursorBrush = SolidColor(headlineColor),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search conversations & messages...",
                                        style = SalimTextStyles.Body,
                                        color = captionColor
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.setSearchQuery("") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = captionColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Folder Filter Pill Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FolderChip(
                    label = "All",
                    isSelected = selectedFolderId == null && !showArchived,
                    isDark = isDark,
                    onClick = {
                        if (showArchived) viewModel.toggleArchivedView()
                        viewModel.selectFolder(null)
                    }
                )

                folders.forEach { folder ->
                    FolderChip(
                        label = folder.name,
                        isSelected = selectedFolderId == folder.id && !showArchived,
                        isDark = isDark,
                        onClick = {
                            if (showArchived) viewModel.toggleArchivedView()
                            viewModel.selectFolder(folder.id)
                        }
                    )
                }

                // Add Folder Chip
                Box(
                    modifier = Modifier
                        .clip(SalimShape)
                        .background(if (isDark) CardSurfaceDark.copy(alpha = 0.6f) else CardSurfaceLight.copy(alpha = 0.6f))
                        .clickable { showCreateFolderDialog = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Folder",
                            tint = captionColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Folder",
                            style = SalimTextStyles.Caption,
                            color = captionColor
                        )
                    }
                }

                // Archive Toggle Chip
                FolderChip(
                    label = "Archived",
                    isSelected = showArchived,
                    isDark = isDark,
                    onClick = { viewModel.toggleArchivedView() }
                )
            }

            // Conversation Cards List
            if (conversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (showArchived) Icons.Default.Archive else Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = captionColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (showArchived) "No archived conversations" else if (searchQuery.isNotEmpty()) "No results found" else "No dialogues yet",
                            style = SalimTextStyles.Title,
                            color = headlineColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try searching for a different phrase" else "Start a new conversation to begin",
                            style = SalimTextStyles.Secondary,
                            color = captionColor
                        )
                        if (!showArchived && searchQuery.isEmpty()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            SalimPrimaryButton(
                                text = "Start conversation",
                                onClick = { viewModel.navigateToNewChat() },
                                isDark = isDark
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(conversations, key = { it.id }) { conversation ->
                        ConversationCardRow(
                            conversation = conversation,
                            folders = folders,
                            isDark = isDark,
                            onClick = { viewModel.navigateToConversation(conversation.id) },
                            onTogglePin = { viewModel.togglePinConversation(conversation) },
                            onToggleArchive = { viewModel.toggleArchiveConversation(conversation) },
                            onDuplicate = { viewModel.duplicateConversation(conversation.id) },
                            onRename = { viewModel.conversationToRename.value = conversation },
                            onDelete = { viewModel.conversationToDelete.value = conversation },
                            onMoveFolder = { folderId -> viewModel.updateFolderForConversation(conversation.id, folderId) }
                        )
                    }
                }
            }
        }
    }

    // Create Folder Dialog
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = {
                Text("New Folder", style = SalimTextStyles.Title, color = headlineColor)
            },
            text = {
                Column {
                    Text(
                        "Create a folder to organize conversations:",
                        style = SalimTextStyles.Secondary,
                        color = captionColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BasicTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SalimShape)
                            .background(if (isDark) CardSurfaceDark else Color(0xFFF2F4EE))
                            .padding(14.dp),
                        textStyle = SalimTextStyles.Body.copy(color = headlineColor),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                SalimPrimaryButton(
                    text = "Create",
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            viewModel.createFolder(newFolderName.trim())
                            newFolderName = ""
                            showCreateFolderDialog = false
                        }
                    },
                    isDark = isDark
                )
            },
            dismissButton = {
                SalimSecondaryButton(
                    text = "Cancel",
                    onClick = { showCreateFolderDialog = false },
                    isDark = isDark
                )
            },
            containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight,
            shape = SalimShape
        )
    }

    // Rename Conversation Dialog
    val convToRename by viewModel.conversationToRename.collectAsState()
    if (convToRename != null) {
        var renameText by remember(convToRename) { mutableStateOf(convToRename?.title ?: "") }
        AlertDialog(
            onDismissRequest = { viewModel.conversationToRename.value = null },
            title = {
                Text("Rename Conversation", style = SalimTextStyles.Title, color = headlineColor)
            },
            text = {
                BasicTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SalimShape)
                        .background(if (isDark) Color(0xFF141715) else Color(0xFFF2F4EE))
                        .padding(14.dp),
                    textStyle = SalimTextStyles.Body.copy(color = headlineColor),
                    singleLine = true
                )
            },
            confirmButton = {
                SalimPrimaryButton(
                    text = "Save",
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameConversation(convToRename!!.id, renameText.trim())
                            viewModel.conversationToRename.value = null
                        }
                    },
                    isDark = isDark
                )
            },
            dismissButton = {
                SalimSecondaryButton(
                    text = "Cancel",
                    onClick = { viewModel.conversationToRename.value = null },
                    isDark = isDark
                )
            },
            containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight,
            shape = SalimShape
        )
    }

    // Delete Confirmation Dialog (naming specific conversation title!)
    val convToDelete by viewModel.conversationToDelete.collectAsState()
    if (convToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.conversationToDelete.value = null },
            title = {
                Text("Delete Conversation?", style = SalimTextStyles.Title, color = headlineColor)
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete \"${convToDelete?.title}\"? All messages within this dialogue will be lost.",
                    style = SalimTextStyles.Body,
                    color = captionColor
                )
            },
            confirmButton = {
                SalimPrimaryButton(
                    text = "Delete",
                    onClick = { viewModel.deleteConversationConfirmed() },
                    isDark = isDark
                )
            },
            dismissButton = {
                SalimSecondaryButton(
                    text = "Cancel",
                    onClick = { viewModel.conversationToDelete.value = null },
                    isDark = isDark
                )
            },
            containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight,
            shape = SalimShape
        )
    }
}

@Composable
private fun FolderChip(
    label: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) {
        if (isDark) Color(0xFFE8EAE4) else Color(0xFF191C1A)
    } else {
        if (isDark) CardSurfaceDark else CardSurfaceLight
    }

    val textColor = if (isSelected) {
        if (isDark) Color(0xFF141715) else Color.White
    } else {
        if (isDark) SecondaryCaptionDark else Color(0xFF4A524A)
    }

    Box(
        modifier = Modifier
            .clip(SalimShape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = SalimTextStyles.Caption,
            color = textColor
        )
    }
}

@Composable
private fun ConversationCardRow(
    conversation: ConversationEntity,
    folders: List<FolderEntity>,
    isDark: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleArchive: () -> Unit,
    onDuplicate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onMoveFolder: (String?) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var folderSubmenuExpanded by remember { mutableStateOf(false) }

    val headlineColor = if (isDark) HeadlineDark else HeadlineLight
    val captionColor = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight
    val folder = folders.find { it.id == conversation.folderId }

    val formattedDate = remember(conversation.updatedAt) {
        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
        sdf.format(Date(conversation.updatedAt))
    }

    SalimCard(
        isDark = isDark,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("conversation_item_${conversation.id}"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small colored icon indicating conversation type or pinned/starred state
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(SalimShape)
                    .background(
                        if (conversation.isPinned) AccentGold.copy(alpha = 0.18f)
                        else if (folder != null) AccentGreen.copy(alpha = 0.18f)
                        else if (isDark) Color(0xFF282C28)
                        else Color(0xFFF0F3EC)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (conversation.isPinned) Icons.Default.PushPin
                    else if (folder != null) Icons.Default.Folder
                    else Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = if (conversation.isPinned) AccentGold
                    else if (folder != null) AccentGreen
                    else captionColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Conversation title and one-line snippet
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.title,
                        style = SalimTextStyles.Body,
                        color = headlineColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (folder != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(SalimShape)
                                .background(AccentGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = folder.name,
                                style = SalimTextStyles.Caption,
                                color = AccentGreen,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (conversation.lastMessageSnippet.isNotBlank()) conversation.lastMessageSnippet else "Tap to open dialogue",
                    style = SalimTextStyles.Secondary,
                    color = captionColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Timestamp and options menu
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formattedDate,
                    style = SalimTextStyles.Caption,
                    color = captionColor
                )

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = captionColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        shape = SalimShape,
                        containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (conversation.isPinned) "Unpin" else "Pin to top") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (conversation.isPinned) Icons.Outlined.PushPin else Icons.Default.PushPin,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onTogglePin()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(if (conversation.isArchived) "Unarchive" else "Archive") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (conversation.isArchived) Icons.Outlined.Unarchive else Icons.Default.Archive,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleArchive()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onDuplicate()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Rename") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onRename()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(if (folder != null) "Remove from Folder" else "Move to Folder") },
                            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                folderSubmenuExpanded = true
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFD32F2F)) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F)) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }

                    // Folder selection submenu
                    DropdownMenu(
                        expanded = folderSubmenuExpanded,
                        onDismissRequest = { folderSubmenuExpanded = false },
                        shape = SalimShape,
                        containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight
                    ) {
                        DropdownMenuItem(
                            text = { Text("None (General)") },
                            onClick = {
                                folderSubmenuExpanded = false
                                onMoveFolder(null)
                            }
                        )
                        folders.forEach { f ->
                            DropdownMenuItem(
                                text = { Text(f.name) },
                                onClick = {
                                    folderSubmenuExpanded = false
                                    onMoveFolder(f.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
