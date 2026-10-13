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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ForkRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.db.MessageEntity
import com.example.data.preferences.AvailableModels
import com.example.ui.components.ArtifactItem
import com.example.ui.components.ArtifactPanelSheet
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
import com.example.ui.viewmodel.TableOfContentsItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
    val templates by viewModel.templates.collectAsState()
    val otherConversations by viewModel.conversationsList.collectAsState()
    val bubbleDensity by viewModel.preferences.bubbleDensity.collectAsState()
    val fontScaleOverride by viewModel.preferences.fontScaleOverride.collectAsState()

    // Jump-to-message In-Chat Search State (Feature 1)
    val isInChatSearching by viewModel.isInChatSearching.collectAsState()
    val inChatSearchQuery by viewModel.inChatSearchQuery.collectAsState()
    val highlightedMessageId by viewModel.highlightedMessageId.collectAsState()

    // Table of contents sheet state (Feature 2)
    val isTocOpen by viewModel.isTableOfContentsOpen.collectAsState()

    // Active artifact panel state (Features 6, 7, 8, 9)
    val activeArtifact by viewModel.activeArtifact.collectAsState()

    // Reporting dialog state (Feature 31)
    val messageToFlag by viewModel.messageToFlag.collectAsState()
    var flagReason by remember { mutableStateOf("Inaccurate information") }

    // Model override dialog state (Feature 15)
    var showModelOverrideDialog by remember { mutableStateOf(false) }

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
    var selectedFilterLevel by remember { mutableStateOf(conversation?.contentFilterLevel ?: "standard") }

    val listState = rememberLazyListState()

    // Autoscroll strictly when user is at the bottom (doesn't fight user's scroll!)
    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) true
            else {
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                lastVisibleItem >= totalItems - 2
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (isAtBottom && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    BackHandler {
        if (isInChatSearching) {
            viewModel.isInChatSearching.value = false
            viewModel.inChatSearchQuery.value = ""
        } else {
            viewModel.navigateBack()
        }
    }

    val headlineColor = if (isDark) HeadlineDark else HeadlineLight
    val captionColor = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight

    var headerMenuExpanded by remember { mutableStateOf(false) }
    var attachmentMenuExpanded by remember { mutableStateOf(false) }

    // In-chat search matched indices calculation
    val inChatMatches = remember(messages, inChatSearchQuery) {
        if (inChatSearchQuery.isBlank()) emptyList()
        else messages.indices.filter { messages[it].content.contains(inChatSearchQuery, ignoreCase = true) }
    }
    var currentMatchIndexPointer by remember(inChatMatches) { mutableIntStateOf(0) }

    // Table of contents outline items calculation (Feature 2)
    val tocItems = remember(messages) {
        val items = mutableListOf<TableOfContentsItem>()
        messages.forEachIndexed { idx, msg ->
            if (msg.sender == "user") {
                val preview = msg.content.take(60).replace("\n", " ").trim()
                if (preview.isNotBlank()) {
                    items.add(TableOfContentsItem(idx, msg.id, preview))
                }
            } else if (msg.content.contains("# ")) {
                val lines = msg.content.lines()
                val heading = lines.firstOrNull { it.startsWith("# ") || it.startsWith("## ") }
                if (heading != null) {
                    val cleanHeading = heading.removePrefix("#").removePrefix("#").trim()
                    items.add(TableOfContentsItem(idx, msg.id, "Section: $cleanHeading"))
                }
            }
        }
        items
    }

    SalimGradientBackground(isDark = isDark) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Header Bar (Normal mode or In-Chat Search mode)
                if (isInChatSearching) {
                    // Feature 1: Jump-to-message Search Bar within open conversation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.isInChatSearching.value = false
                                viewModel.inChatSearchQuery.value = ""
                                viewModel.highlightedMessageId.value = null
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Exit search", tint = headlineColor)
                        }

                        BasicTextField(
                            value = inChatSearchQuery,
                            onValueChange = {
                                viewModel.inChatSearchQuery.value = it
                                currentMatchIndexPointer = 0
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clip(SalimShape)
                                .background(if (isDark) CardSurfaceDark else CardSurfaceLight)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            textStyle = SalimTextStyles.Body.copy(color = headlineColor),
                            cursorBrush = SolidColor(headlineColor),
                            singleLine = true,
                            decorationBox = { inner ->
                                if (inChatSearchQuery.isEmpty()) {
                                    Text("Find in dialogue...", style = SalimTextStyles.Body, color = captionColor)
                                }
                                inner()
                            }
                        )

                        if (inChatMatches.isNotEmpty()) {
                            Text(
                                text = "${currentMatchIndexPointer + 1}/${inChatMatches.size}",
                                style = SalimTextStyles.Caption,
                                color = captionColor,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )

                            IconButton(
                                onClick = {
                                    if (currentMatchIndexPointer > 0) {
                                        currentMatchIndexPointer--
                                    } else {
                                        currentMatchIndexPointer = inChatMatches.size - 1
                                    }
                                    val targetIdx = inChatMatches[currentMatchIndexPointer]
                                    viewModel.highlightedMessageId.value = messages[targetIdx].id
                                    scope.launch { listState.animateScrollToItem(targetIdx) }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous match", tint = headlineColor)
                            }

                            IconButton(
                                onClick = {
                                    if (currentMatchIndexPointer < inChatMatches.size - 1) {
                                        currentMatchIndexPointer++
                                    } else {
                                        currentMatchIndexPointer = 0
                                    }
                                    val targetIdx = inChatMatches[currentMatchIndexPointer]
                                    viewModel.highlightedMessageId.value = messages[targetIdx].id
                                    scope.launch { listState.animateScrollToItem(targetIdx) }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next match", tint = headlineColor)
                            }
                        }
                    }
                } else {
                    // Standard Header Bar
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
                            val modelOverride = AvailableModels.find { it.id == conversation?.modelId }?.displayName
                            Text(
                                text = if (isGenerating) "Formulating response..." else (modelOverride ?: "Tap to rename dialogue"),
                                style = SalimTextStyles.Caption,
                                color = if (isGenerating) AccentGreen else captionColor
                            )
                        }

                        // Jump-to-message In-Chat Search Icon
                        IconButton(
                            onClick = { viewModel.isInChatSearching.value = true },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Find in dialogue", tint = headlineColor)
                        }

                        // Table of Contents Outline Button
                        IconButton(
                            onClick = { viewModel.isTableOfContentsOpen.value = true },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Table of contents", tint = headlineColor)
                        }

                        // Persistent New Chat Control
                        IconButton(
                            onClick = { viewModel.navigateToNewChat() },
                            modifier = Modifier
                                .size(44.dp)
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
                                    .size(44.dp)
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
                                    text = { Text("Summarize Dialogue (On Demand)") },
                                    leadingIcon = { Icon(Icons.Default.Summarize, contentDescription = null) },
                                    onClick = {
                                        headerMenuExpanded = false
                                        viewModel.summarizeCurrentConversation()
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Engine Override") },
                                    leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null) },
                                    onClick = {
                                        headerMenuExpanded = false
                                        showModelOverrideDialog = true
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text(if (showTimestamps) "Hide Timestamps" else "Show Timestamps") },
                                    onClick = {
                                        headerMenuExpanded = false
                                        viewModel.showTimestamps.value = !showTimestamps
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Conversation Directives & Sensitivity") },
                                    leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null) },
                                    onClick = {
                                        headerMenuExpanded = false
                                        instructionsText = conversation?.customInstructions ?: ""
                                        selectedFilterLevel = conversation?.contentFilterLevel ?: "standard"
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
                                    text = { Text("Duplicate Dialogue") },
                                    leadingIcon = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = null) },
                                    onClick = {
                                        headerMenuExpanded = false
                                        viewModel.duplicateConversation(conversationId)
                                    }
                                )
                            }
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
                    verticalArrangement = Arrangement.spacedBy(
                        when (bubbleDensity) {
                            "compact" -> 8.dp
                            "spacious" -> 22.dp
                            else -> 14.dp
                        }
                    )
                ) {
                    itemsIndexed(messages, key = { _, it -> it.id }) { _, message ->
                        val isStreamingThis = message.id == streamingId
                        val displayContent = if (isStreamingThis && streamingContent.isNotEmpty()) {
                            streamingContent
                        } else {
                            message.content
                        }
                        val isHighlighted = message.id == highlightedMessageId

                        if (message.sender == "user") {
                            UserMessageBubble(
                                message = message,
                                content = displayContent,
                                showTimestamp = showTimestamps,
                                isHighlighted = isHighlighted,
                                isDark = isDark,
                                density = bubbleDensity,
                                fontScale = fontScaleOverride,
                                onCopy = {
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cb.setPrimaryClip(ClipData.newPlainText("Salim User Message", message.content))
                                    Toast.makeText(context, "Copied prompt", Toast.LENGTH_SHORT).show()
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
                                isHighlighted = isHighlighted,
                                isDark = isDark,
                                density = bubbleDensity,
                                fontScale = fontScaleOverride,
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
                                },
                                onOpenArtifact = { artTitle, artType, artLang, artCode ->
                                    viewModel.openArtifact(artTitle, artType, artLang, artCode)
                                },
                                onFlagMessage = {
                                    viewModel.messageToFlag.value = message
                                }
                            )
                        }
                    }
                }

                // Autocomplete Bar for Slash Commands (Feature 10) & @-Mentions (Feature 11)
                if (inputText.startsWith("/")) {
                    val typedCmd = inputText.removePrefix("/").lowercase()
                    val matchingTemplates = templates.filter {
                        it.shortcut.lowercase().contains(typedCmd) || it.title.lowercase().contains(typedCmd)
                    }
                    if (matchingTemplates.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            matchingTemplates.forEach { t ->
                                Box(
                                    modifier = Modifier
                                        .clip(SalimShape)
                                        .background(if (isDark) CardSurfaceDark else CardSurfaceLight)
                                        .clickable {
                                            viewModel.inputText.value = t.content
                                        }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${t.shortcut} — ${t.title}",
                                        style = SalimTextStyles.Caption,
                                        color = if (isDark) HeadlineDark else HeadlineLight
                                    )
                                }
                            }
                        }
                    }
                } else if (inputText.contains("@")) {
                    val query = inputText.substringAfterLast("@").lowercase()
                    val matchingDialogues = otherConversations.filter {
                        it.title.lowercase().contains(query) && it.id != conversationId
                    }.take(4)
                    if (matchingDialogues.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            matchingDialogues.forEach { d ->
                                Box(
                                    modifier = Modifier
                                        .clip(SalimShape)
                                        .background(if (isDark) CardSurfaceDark else CardSurfaceLight)
                                        .clickable {
                                            val before = inputText.substringBeforeLast("@")
                                            viewModel.inputText.value = "$before[Ref: ${d.title}] "
                                        }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "@${d.title}",
                                        style = SalimTextStyles.Caption,
                                        color = AccentGreen
                                    )
                                }
                            }
                        }
                    }
                }

                // Active Attachment Preview in Composer
                AnimatedVisibility(visible = activeAttachment != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
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
                                        text = { Text("Paste Clipboard") },
                                        leadingIcon = { Icon(Icons.Default.ContentPaste, contentDescription = null) },
                                        onClick = {
                                            attachmentMenuExpanded = false
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = clipboard.primaryClip
                                            if (clip != null && clip.itemCount > 0) {
                                                val text = clip.getItemAt(0).text?.toString() ?: ""
                                                val uri = clip.getItemAt(0).uri
                                                if (uri != null) {
                                                    viewModel.setAttachmentFromUri(uri, "image", "Pasted_Image.jpg")
                                                } else if (text.isNotBlank()) {
                                                    viewModel.inputText.value = if (viewModel.inputText.value.isEmpty()) text else "${viewModel.inputText.value} $text"
                                                }
                                            }
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

                            // Auto-resizing Multi-line Input Field (Feature 13 & Keyboard Parity Feature 14)
                            BasicTextField(
                                value = inputText,
                                onValueChange = { viewModel.inputText.value = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp, vertical = 12.dp)
                                    .heightIn(min = 24.dp, max = 135.dp)
                                    .testTag("chat_input_field"),
                                textStyle = SalimTextStyles.Body.copy(
                                    color = headlineColor,
                                    fontSize = (17 * fontScaleOverride).sp
                                ),
                                cursorBrush = SolidColor(headlineColor),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = if (enterToSend) ImeAction.Send else ImeAction.Default
                                ),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (enterToSend) viewModel.sendMessage()
                                    }
                                ),
                                decorationBox = { innerTextField ->
                                    if (inputText.isEmpty() && activeAttachment == null) {
                                        Text(
                                            text = "Message Salim (type / or @)...",
                                            style = SalimTextStyles.Body.copy(fontSize = (17 * fontScaleOverride).sp),
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

                            // Responsive Send / Stop Button: Parity between keyboard action and UI button
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

            // Feature 4: Scroll-to-bottom / "Jump to latest" floating control
            // Only appears when user has scrolled up from the bottom!
            if (!isAtBottom && messages.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = 90.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(SalimShape)
                            .background(if (isDark) Color(0xFFE8EAE4) else Color(0xFF191C1A))
                            .clickable {
                                scope.launch { listState.animateScrollToItem(messages.size - 1) }
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Jump to latest",
                                tint = if (isDark) Color(0xFF141715) else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Latest",
                                style = SalimTextStyles.Caption,
                                color = if (isDark) Color(0xFF141715) else Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Feature 2: Table of Contents Sheet
    if (isTocOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.isTableOfContentsOpen.value = false },
            containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight,
            shape = SalimShape
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dialogue Outline", style = SalimTextStyles.Title, color = headlineColor)
                    IconButton(onClick = { viewModel.isTableOfContentsOpen.value = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = captionColor)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (tocItems.isEmpty()) {
                    Text(
                        "No sections yet. As questions and topic shifts occur, outline markers will appear here.",
                        style = SalimTextStyles.Secondary,
                        color = captionColor,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(tocItems) { _, item ->
                            SalimCard(
                                isDark = isDark,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    viewModel.isTableOfContentsOpen.value = false
                                    viewModel.highlightedMessageId.value = item.messageId
                                    scope.launch { listState.animateScrollToItem(item.messageIndex) }
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(AccentGreen.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${item.messageIndex + 1}",
                                            style = SalimTextStyles.Caption,
                                            color = AccentGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = item.label,
                                        style = SalimTextStyles.Secondary,
                                        color = headlineColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Feature 6, 7, 8, 9: Artifact Side-Panel Sheet
    if (activeArtifact != null) {
        ArtifactPanelSheet(
            artifact = activeArtifact!!,
            isDark = isDark,
            onDismiss = { viewModel.activeArtifact.value = null },
            onSendBackAsContext = { updatedContext ->
                viewModel.inputText.value = updatedContext
            }
        )
    }

    // Feature 31: Report / Flag Response Dialog
    if (messageToFlag != null) {
        val reasons = listOf("Inaccurate information", "Harmful or unsafe", "Offensive or rude", "Hallucinated or broken code", "Other")
        AlertDialog(
            onDismissRequest = { viewModel.messageToFlag.value = null },
            title = {
                Text("Report Response", style = SalimTextStyles.Title, color = headlineColor)
            },
            text = {
                Column {
                    Text(
                        "Reports are securely logged to your local device audit record for inspection.",
                        style = SalimTextStyles.Caption,
                        color = captionColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    reasons.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { flagReason = r }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = flagReason == r,
                                onClick = { flagReason = r },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = if (isDark) AccentGold else AccentGreen
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = r, style = SalimTextStyles.Secondary, color = headlineColor)
                        }
                    }
                }
            },
            confirmButton = {
                SalimPrimaryButton(
                    text = "Submit Report",
                    onClick = {
                        val msg = messageToFlag
                        if (msg != null) {
                            viewModel.flagMessage(msg.id, flagReason)
                            Toast.makeText(context, "Response logged in local report audit", Toast.LENGTH_SHORT).show()
                        }
                    },
                    isDark = isDark
                )
            },
            dismissButton = {
                SalimSecondaryButton(
                    text = "Cancel",
                    onClick = { viewModel.messageToFlag.value = null },
                    isDark = isDark
                )
            },
            containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight,
            shape = SalimShape
        )
    }

    // Feature 15: Per-Conversation Model Override Dialog
    if (showModelOverrideDialog) {
        AlertDialog(
            onDismissRequest = { showModelOverrideDialog = false },
            title = {
                Text("Dialogue Engine Override", style = SalimTextStyles.Title, color = headlineColor)
            },
            text = {
                Column {
                    Text(
                        "Pin this dialogue to a specific model to maintain behavioral consistency:",
                        style = SalimTextStyles.Caption,
                        color = captionColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AvailableModels.forEach { m ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateModelForConversation(conversationId, m.id)
                                    showModelOverrideDialog = false
                                }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = conversation?.modelId == m.id || (conversation?.modelId == null && m.id == viewModel.preferences.selectedModel.value),
                                onClick = {
                                    viewModel.updateModelForConversation(conversationId, m.id)
                                    showModelOverrideDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = if (isDark) AccentGold else AccentGreen
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = m.displayName, style = SalimTextStyles.Secondary.copy(fontWeight = FontWeight.SemiBold), color = headlineColor)
                                Text(text = m.badge, style = SalimTextStyles.Caption, color = AccentGreen)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                SalimSecondaryButton(
                    text = "Close",
                    onClick = { showModelOverrideDialog = false },
                    isDark = isDark
                )
            },
            containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight,
            shape = SalimShape
        )
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

    // Per-conversation instructions & content filter sensitivity dialog
    if (showInstructionsDialog) {
        AlertDialog(
            onDismissRequest = { showInstructionsDialog = false },
            title = {
                Text("Conversation Directives", style = SalimTextStyles.Title, color = headlineColor)
            },
            text = {
                Column {
                    Text("Directives:", style = SalimTextStyles.Caption, color = captionColor)
                    Spacer(modifier = Modifier.height(6.dp))
                    BasicTextField(
                        value = instructionsText,
                        onValueChange = { instructionsText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 70.dp, max = 130.dp)
                            .clip(SalimShape)
                            .background(if (isDark) Color(0xFF141715) else Color(0xFFF0F3EC))
                            .padding(12.dp),
                        textStyle = SalimTextStyles.Body.copy(color = headlineColor)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Content Filtering Posture:", style = SalimTextStyles.Caption, color = captionColor)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("standard", "relaxed", "strict").forEach { lvl ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { selectedFilterLevel = lvl }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = selectedFilterLevel == lvl,
                                    onClick = { selectedFilterLevel = lvl },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = if (isDark) AccentGold else AccentGreen
                                    )
                                )
                                Text(lvl.replaceFirstChar { it.uppercase() }, style = SalimTextStyles.Caption, color = headlineColor)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                SalimPrimaryButton(
                    text = "Apply",
                    onClick = {
                        scope.launch {
                            viewModel.repository.updateCustomInstructions(conversationId, instructionsText.trim())
                            viewModel.repository.updateContentFilterLevel(conversationId, selectedFilterLevel)
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
 * User message bubble with density and font-scale support.
 */
@Composable
private fun UserMessageBubble(
    message: MessageEntity,
    content: String,
    showTimestamp: Boolean,
    isHighlighted: Boolean,
    isDark: Boolean,
    density: String,
    fontScale: Float,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onBranch: () -> Unit
) {
    val bgColor = if (isDark) UserBubbleBgDark else UserBubbleBgLight
    val textColor = if (isDark) UserBubbleTextDark else UserBubbleTextLight

    var showActions by remember { mutableStateOf(false) }

    val padH = when (density) { "compact" -> 14.dp; "spacious" -> 22.dp; else -> 18.dp }
    val padV = when (density) { "compact" -> 8.dp; "spacious" -> 16.dp; else -> 12.dp }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
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

        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(SalimShape)
                .background(bgColor)
                .then(
                    if (isHighlighted) Modifier.border(2.dp, AccentGold, SalimShape)
                    else Modifier
                )
                .clickable { showActions = !showActions }
                .padding(horizontal = padH, vertical = padV)
        ) {
            Text(
                text = content,
                style = SalimTextStyles.Body.copy(fontSize = (17 * fontScale).sp),
                color = textColor
            )
        }

        AnimatedVisibility(visible = showActions) {
            Row(
                modifier = Modifier.padding(top = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy message", tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit message", tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onBranch, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Outlined.ForkRight, contentDescription = "Branch dialogue", tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight, modifier = Modifier.size(16.dp))
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp, end = 6.dp)
        ) {
            if (showTimestamp) {
                val formattedTime = remember(message.createdAt) {
                    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.createdAt))
                }
                Text(
                    text = formattedTime,
                    style = SalimTextStyles.Caption,
                    color = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }

            val statusIcon = when (message.deliveryStatus) {
                "sending" -> Icons.Default.Schedule
                "failed" -> Icons.Default.ErrorOutline
                else -> Icons.Default.DoneAll
            }
            val statusTint = when (message.deliveryStatus) {
                "sending" -> (if (isDark) SecondaryCaptionDark else SecondaryCaptionLight).copy(alpha = 0.5f)
                "failed" -> Color(0xFFD32F2F)
                else -> AccentGreen
            }

            Icon(
                imageVector = statusIcon,
                contentDescription = "Delivery status: ${message.deliveryStatus}",
                tint = statusTint,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

/**
 * Assistant message bubble with artifacts detector, density, and font-scale support.
 */
@Composable
private fun AssistantMessageBubble(
    message: MessageEntity,
    content: String,
    isStreaming: Boolean,
    showTimestamp: Boolean,
    showTokens: Boolean,
    isHighlighted: Boolean,
    isDark: Boolean,
    density: String,
    fontScale: Float,
    onCopy: () -> Unit,
    onStarToggle: () -> Unit,
    onRegenerate: () -> Unit,
    onSpeak: () -> Unit,
    onDeletePair: () -> Unit,
    onOpenArtifact: (title: String, type: String, language: String, content: String) -> Unit,
    onFlagMessage: () -> Unit
) {
    val bgColor = if (isDark) AssistantBubbleBgDark else AssistantBubbleBgLight
    val textColor = if (isDark) AssistantBubbleTextDark else AssistantBubbleTextLight

    var showLatencyFloor by remember { mutableStateOf(false) }
    LaunchedEffect(isStreaming, content.isNotEmpty()) {
        if (isStreaming && content.isEmpty()) {
            delay(400)
            showLatencyFloor = true
        } else {
            showLatencyFloor = false
        }
    }

    val padH = when (density) { "compact" -> 14.dp; "spacious" -> 22.dp; else -> 18.dp }
    val padV = when (density) { "compact" -> 10.dp; "spacious" -> 18.dp; else -> 14.dp }

    // Check if message content contains substantial code/data block for Artifact extraction (Feature 6)
    val hasCodeBlock = remember(content) {
        content.contains("```") && content.length > 120
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .clip(SalimShape)
                .background(bgColor)
                .then(
                    if (isHighlighted) Modifier.border(2.dp, AccentGold, SalimShape)
                    else Modifier
                )
                .padding(horizontal = padH, vertical = padV)
        ) {
            Column {
                if (content.isBlank() && isStreaming) {
                    if (showLatencyFloor) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(AccentGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Formulating response...",
                                style = SalimTextStyles.Secondary,
                                color = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                } else {
                    SalimMarkdownRenderer(
                        content = content,
                        textColor = textColor,
                        isDark = isDark
                    )

                    // Artifact Badge Card if response contains substantial code / output
                    if (hasCodeBlock && !isStreaming) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SalimShape)
                                .background(if (isDark) Color(0xFF141715) else Color(0xFFF0F3EC))
                                .clickable {
                                    val lang = content.substringAfter("```").substringBefore("\n").trim().ifEmpty { "code" }
                                    val codeBody = content.substringAfter("```$lang").substringBefore("```").trim()
                                    onOpenArtifact("Generated $lang Output", "code", lang, codeBody)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Open in Artifact Panel",
                                    style = SalimTextStyles.Caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = AccentGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        // Toolbar below response: Copy, Star, Speak, Regenerate, Flag, Delete pair
        Row(
            modifier = Modifier.padding(top = 6.dp, start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy response", tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight, modifier = Modifier.size(16.dp))
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
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Read aloud", tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight, modifier = Modifier.size(16.dp))
            }

            if (!isStreaming) {
                IconButton(onClick = onRegenerate, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Regenerate response", tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight, modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onFlagMessage, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Flag, contentDescription = "Report response", tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight, modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onDeletePair, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete exchange", tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight, modifier = Modifier.size(16.dp))
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
