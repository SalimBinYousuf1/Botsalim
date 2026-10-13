package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ForkRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.db.MessageEntity
import com.example.ui.components.SalimMarkdownRenderer
import com.example.ui.components.TtsHelper
import com.example.ui.components.VoiceInputHelper
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AssistantBubbleBgDark
import com.example.ui.theme.AssistantBubbleBgLight
import com.example.ui.theme.AssistantBubbleTextDark
import com.example.ui.theme.AssistantBubbleTextLight
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
import com.example.ui.theme.UserBubbleBgDark
import com.example.ui.theme.UserBubbleBgLight
import com.example.ui.theme.UserBubbleTextDark
import com.example.ui.theme.UserBubbleTextLight
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    conversationId: String,
    viewModel: MainViewModel,
    isDark: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val conversation by viewModel.currentConversation.collectAsState()
    val messages by viewModel.currentMessages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val activeAttachment by viewModel.activeAttachment.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val streamingId by viewModel.streamingMessageId.collectAsState()
    val streamingContent by viewModel.streamingContent.collectAsState()
    val showTimestamps by viewModel.showTimestamps.collectAsState()
    val showTokenUsage by viewModel.preferences.showTokenUsage.collectAsState()
    val enterToSend by viewModel.preferences.enterToSend.collectAsState()

    val ttsHelper = remember { TtsHelper(context) }
    DisposableEffect(Unit) {
        onDispose { ttsHelper.shutdown() }
    }

    var isListeningVoice by remember { mutableStateOf(false) }
    val voiceHelper = remember {
        VoiceInputHelper(context) { dictatedText ->
            if (dictatedText.isNotBlank()) {
                viewModel.inputText.value = if (viewModel.inputText.value.isEmpty()) {
                    dictatedText
                } else {
                    "${viewModel.inputText.value} $dictatedText"
                }
            }
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isListeningVoice = true
            voiceHelper.startListening()
        } else {
            Toast.makeText(context, "Microphone permission needed for voice input", Toast.LENGTH_SHORT).show()
        }
    }

    // Media pickers
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.setAttachmentFromUri(uri, "image", "Image_${System.currentTimeMillis()}.jpg")
        }
    }

    val documentPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.setAttachmentFromUri(uri, "document", "Document_${System.currentTimeMillis()}")
        }
    }

    // Message edit state
    var editingMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var editContentText by remember { mutableStateOf("") }

    // Per-conversation instructions dialog
    var showInstructionsDialog by remember { mutableStateOf(false) }
    var instructionsText by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size, streamingContent.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    BackHandler {
        viewModel.navigateBack()
    }

    val headlineColor = if (isDark) HeadlineDark else HeadlineLight
    val captionColor = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight

    var headerMenuExpanded by remember { mutableStateOf(false) }
    var attachmentMenuExpanded by remember { mutableStateOf(false) }

    SalimGradientBackground(isDark = isDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header Bar sitting directly on the gradient
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("chat_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = headlineColor
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.conversationToRename.value = conversation }
                ) {
                    Text(
                        text = conversation?.title ?: "Salim",
                        style = SalimTextStyles.Title,
                        color = headlineColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isGenerating) "Formulating response..." else "Tap to rename dialogue",
                        style = SalimTextStyles.Caption,
                        color = if (isGenerating) AccentGreen else captionColor
                    )
                }

                // Persistent New Chat Control (visible inside conversation!)
                IconButton(
                    onClick = { viewModel.navigateToNewChat() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("chat_new_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = headlineColor
                    )
                }

                // Overflow Options Menu
                Box {
                    IconButton(
                        onClick = { headerMenuExpanded = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("chat_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Conversation Options",
                            tint = headlineColor
                        )
                    }

                    DropdownMenu(
                        expanded = headerMenuExpanded,
                        onDismissRequest = { headerMenuExpanded = false },
                        shape = SalimShape,
                        containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (showTimestamps) "Hide Timestamps" else "Show Timestamps") },
                            onClick = {
                                headerMenuExpanded = false
                                viewModel.showTimestamps.value = !showTimestamps
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Conversation Directives") },
                            leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null) },
                            onClick = {
                                headerMenuExpanded = false
                                instructionsText = conversation?.customInstructions ?: ""
                                showInstructionsDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Export as Markdown") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                headerMenuExpanded = false
                                scope.launch {
                                    val exported = viewModel.repository.exportConversation(conversationId, asMarkdown = true)
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, exported)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Export Conversation"))
                                }
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Export as Plain Text") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                headerMenuExpanded = false
                                scope.launch {
                                    val exported = viewModel.repository.exportConversation(conversationId, asMarkdown = false)
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, exported)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Export Conversation"))
                                }
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Duplicate Conversation") },
                            leadingIcon = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = null) },
                            onClick = {
                                headerMenuExpanded = false
                                viewModel.duplicateConversation(conversationId)
                            }
                        )
                    }
                }
            }

            // Messages Stream / Conversation History
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    val isStreamingThis = message.id == streamingId
                    val displayContent = if (isStreamingThis && streamingContent.isNotEmpty()) {
                        streamingContent
                    } else {
                        message.content
                    }

                    if (message.sender == "user") {
                        UserMessageBubble(
                            message = message,
                            content = displayContent,
                            showTimestamp = showTimestamps,
                            isDark = isDark,
                            onCopy = {
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cb.setPrimaryClip(ClipData.newPlainText("Salim User Message", message.content))
                                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                            },
                            onEdit = {
                                editingMessage = message
                                editContentText = message.content
                            },
                            onBranch = {
                                viewModel.branchFromMessage(message.id)
                            }
                        )
                    } else {
                        AssistantMessageBubble(
                            message = message,
                            content = displayContent,
                            isStreaming = isStreamingThis && isGenerating,
                            showTimestamp = showTimestamps,
                            showTokens = showTokenUsage,
                            isDark = isDark,
                            onCopy = {
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cb.setPrimaryClip(ClipData.newPlainText("Salim Response", displayContent))
                                Toast.makeText(context, "Copied response", Toast.LENGTH_SHORT).show()
                            },
                            onStarToggle = {
                                viewModel.toggleStarMessage(message.id, message.isStarred)
                            },
                            onRegenerate = {
                                viewModel.regenerateMessage(message.id)
                            },
                            onSpeak = {
                                ttsHelper.speak(message.id, displayContent)
                            },
                            onDeletePair = {
                                val idx = messages.indexOf(message)
                                if (idx > 0 && messages[idx - 1].sender == "user") {
                                    viewModel.deleteMessagePair(messages[idx - 1].id, message.id)
                                } else {
                                    viewModel.deleteMessage(message.id)
                                }
                            }
                        )
                    }
                }
            }

            // Active Attachment Preview in Composer
            AnimatedVisibility(visible = activeAttachment != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(SalimShape)
                        .background(if (isDark) CardSurfaceDark else CardSurfaceLight)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeAttachment?.type == "image" && activeAttachment?.uri != null) {
                        AsyncImage(
                            model = activeAttachment?.uri,
                            contentDescription = "Attachment preview",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(SalimShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = activeAttachment?.name ?: "Attached file",
                        style = SalimTextStyles.Caption,
                        color = headlineColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.removeAttachment() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove attachment",
                            tint = captionColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Fixed Bottom Input Bar: Flat white bar with rounded multi-line text field and circular send button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SalimCard(
                    isDark = isDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Attachment (+) Button
                        Box {
                            IconButton(
                                onClick = { attachmentMenuExpanded = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("attachment_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add attachment or template",
                                    tint = captionColor
                                )
                            }

                            DropdownMenu(
                                expanded = attachmentMenuExpanded,
                                onDismissRequest = { attachmentMenuExpanded = false },
                                shape = SalimShape,
                                containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Upload Image") },
                                    leadingIcon = { Icon(Icons.Default.AttachFile, contentDescription = null) },
                                    onClick = {
                                        attachmentMenuExpanded = false
                                        imagePicker.launch("image/*")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Upload Document (TXT/MD/CSV)") },
                                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                                    onClick = {
                                        attachmentMenuExpanded = false
                                        documentPicker.launch("*/*")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Prompt Templates") },
                                    leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null) },
                                    onClick = {
                                        attachmentMenuExpanded = false
                                        viewModel.isTemplatesSheetOpen.value = true
                                    }
                                )
                            }
                        }

                        // Rounded Multi-line Input Field
                        BasicTextField(
                            value = inputText,
                            onValueChange = { viewModel.inputText.value = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp, vertical = 12.dp)
                                .heightIn(min = 24.dp, max = 130.dp)
                                .testTag("chat_input_field"),
                            textStyle = SalimTextStyles.Body.copy(color = headlineColor),
                            cursorBrush = SolidColor(headlineColor),
                            decorationBox = { innerTextField ->
                                if (inputText.isEmpty() && activeAttachment == null) {
                                    Text(
                                        text = "Message Salim...",
                                        style = SalimTextStyles.Body,
                                        color = captionColor
                                    )
                                }
                                innerTextField()
                            }
                        )

                        // Voice Dictation Button
                        IconButton(
                            onClick = {
                                if (isListeningVoice) {
                                    voiceHelper.stopListening()
                                    isListeningVoice = false
                                } else {
                                    if (ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        isListeningVoice = true
                                        voiceHelper.startListening()
                                    } else {
                                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("voice_input_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = if (isListeningVoice) "Listening..." else "Voice dictation",
                                tint = if (isListeningVoice) AccentGreen else captionColor
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Responsive Send / Stop Button: Black circular send button matching reference image
                        if (isGenerating) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD32F2F))
                                    .clickable { viewModel.stopGeneration() }
                                    .testTag("stop_generation_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop generation",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            val canSend = inputText.isNotBlank() || activeAttachment != null
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (canSend) {
                                            if (isDark) Color(0xFFE8EAE4) else Color(0xFF191C1A)
                                        } else {
                                            (if (isDark) Color(0xFFE8EAE4) else Color(0xFF191C1A)).copy(alpha = 0.35f)
                                        }
                                    )
                                    .clickable(enabled = canSend) {
                                        viewModel.sendMessage()
                                    }
                                    .testTag("send_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send message",
                                    tint = if (isDark) Color(0xFF141715) else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit message dialog
    if (editingMessage != null) {
        AlertDialog(
            onDismissRequest = { editingMessage = null },
            title = {
                Text("Edit Message", style = SalimTextStyles.Title, color = headlineColor)
            },
            text = {
                Column {
                    Text(
                        "Editing this prompt creates a new branch and regenerates the response:",
                        style = SalimTextStyles.Caption,
                        color = captionColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    BasicTextField(
                        value = editContentText,
                        onValueChange = { editContentText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp, max = 200.dp)
                            .clip(SalimShape)
                            .background(if (isDark) Color(0xFF141715) else Color(0xFFF0F3EC))
                            .padding(14.dp),
                        textStyle = SalimTextStyles.Body.copy(color = headlineColor)
                    )
                }
            },
            confirmButton = {
                SalimPrimaryButton(
                    text = "Save & Branch",
                    onClick = {
                        val msg = editingMessage
                        if (msg != null && editContentText.isNotBlank()) {
                            viewModel.editMessage(msg.id, editContentText.trim())
                            editingMessage = null
                        }
                    },
                    isDark = isDark
                )
            },
            dismissButton = {
                SalimSecondaryButton(
                    text = "Cancel",
                    onClick = { editingMessage = null },
                    isDark = isDark
                )
            },
            containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight,
            shape = SalimShape
        )
    }

    // Per-conversation instructions override dialog
    if (showInstructionsDialog) {
        AlertDialog(
            onDismissRequest = { showInstructionsDialog = false },
            title = {
                Text("Conversation Directives", style = SalimTextStyles.Title, color = headlineColor)
            },
            text = {
                Column {
                    Text(
                        "Custom instructions specific to this dialogue:",
                        style = SalimTextStyles.Caption,
                        color = captionColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    BasicTextField(
                        value = instructionsText,
                        onValueChange = { instructionsText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp, max = 160.dp)
                            .clip(SalimShape)
                            .background(if (isDark) Color(0xFF141715) else Color(0xFFF0F3EC))
                            .padding(14.dp),
                        textStyle = SalimTextStyles.Body.copy(color = headlineColor)
                    )
                }
            },
            confirmButton = {
                SalimPrimaryButton(
                    text = "Apply",
                    onClick = {
                        scope.launch {
                            viewModel.repository.updateCustomInstructions(conversationId, instructionsText.trim())
                            showInstructionsDialog = false
                        }
                    },
                    isDark = isDark
                )
            },
            dismissButton = {
                SalimSecondaryButton(
                    text = "Cancel",
                    onClick = { showInstructionsDialog = false },
                    isDark = isDark
                )
            },
            containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight,
            shape = SalimShape
        )
    }
}

/**
 * User message bubble: solid near-black pill bubble (22dp) with white text, aligned right.
 */
@Composable
private fun UserMessageBubble(
    message: MessageEntity,
    content: String,
    showTimestamp: Boolean,
    isDark: Boolean,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onBranch: () -> Unit
) {
    val bgColor = if (isDark) UserBubbleBgDark else UserBubbleBgLight
    val textColor = if (isDark) UserBubbleTextDark else UserBubbleTextLight

    var showActions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        // Branch selector if branched
        if (message.totalBranches > 1) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp, end = 6.dp)
            ) {
                Text(
                    text = "Branch ${message.branchIndex + 1}/${message.totalBranches}",
                    style = SalimTextStyles.Caption,
                    color = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight
                )
            }
        }

        // Attachment thumbnail if present
        if (message.attachmentUri != null) {
            if (message.attachmentType == "image") {
                AsyncImage(
                    model = message.attachmentUri,
                    contentDescription = "Attached Image",
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .size(180.dp)
                        .clip(SalimShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Row(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .clip(SalimShape)
                        .background(bgColor.copy(alpha = 0.8f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = message.attachmentName ?: "Attachment",
                        style = SalimTextStyles.Caption,
                        color = textColor
                    )
                }
            }
        }

        // Bubble surface
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(SalimShape)
                .background(bgColor)
                .clickable { showActions = !showActions }
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Text(
                text = content,
                style = SalimTextStyles.Body,
                color = textColor
            )
        }

        // Action controls (Copy, Edit, Branch)
        AnimatedVisibility(visible = showActions) {
            Row(
                modifier = Modifier.padding(top = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy message",
                        tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit message",
                        tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onBranch, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.ForkRight,
                        contentDescription = "Branch dialogue",
                        tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Timestamp
        if (showTimestamp) {
            val formattedTime = remember(message.createdAt) {
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.createdAt))
            }
            Text(
                text = formattedTime,
                style = SalimTextStyles.Caption,
                color = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight,
                modifier = Modifier.padding(top = 4.dp, end = 6.dp)
            )
        }
    }
}

/**
 * Assistant message bubble: white rounded-card bubble (22dp) with dark text, aligned left.
 */
@Composable
private fun AssistantMessageBubble(
    message: MessageEntity,
    content: String,
    isStreaming: Boolean,
    showTimestamp: Boolean,
    showTokens: Boolean,
    isDark: Boolean,
    onCopy: () -> Unit,
    onStarToggle: () -> Unit,
    onRegenerate: () -> Unit,
    onSpeak: () -> Unit,
    onDeletePair: () -> Unit
) {
    val bgColor = if (isDark) AssistantBubbleBgDark else AssistantBubbleBgLight
    val textColor = if (isDark) AssistantBubbleTextDark else AssistantBubbleTextLight

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Card Bubble sitting on gradient
        Box(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .clip(SalimShape)
                .background(bgColor)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Column {
                if (content.isBlank() && isStreaming) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = AccentGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thinking...",
                            style = SalimTextStyles.Secondary,
                            color = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight
                        )
                    }
                } else {
                    SalimMarkdownRenderer(
                        content = content,
                        textColor = textColor,
                        isDark = isDark
                    )
                }
            }
        }

        // Toolbar below assistant response: Copy, Star, Speak, Regenerate, Delete pair
        Row(
            modifier = Modifier.padding(top = 6.dp, start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy response",
                    tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(onClick = onStarToggle, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (message.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (message.isStarred) "Starred" else "Star message",
                    tint = if (message.isStarred) AccentGold else (if (isDark) SecondaryCaptionDark else SecondaryCaptionLight),
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(onClick = onSpeak, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Read aloud",
                    tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (!isStreaming) {
                IconButton(onClick = onRegenerate, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Regenerate response",
                        tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(onClick = onDeletePair, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete exchange",
                        tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (showTokens && message.tokenCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${message.tokenCount} tokens",
                    style = SalimTextStyles.Caption,
                    color = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight
                )
            }

            if (showTimestamp) {
                Spacer(modifier = Modifier.width(6.dp))
                val formattedTime = remember(message.createdAt) {
                    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.createdAt))
                }
                Text(
                    text = formattedTime,
                    style = SalimTextStyles.Caption,
                    color = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight
                )
            }
        }
    }
}
