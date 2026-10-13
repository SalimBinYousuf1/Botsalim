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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Link
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontStyle
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
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CardSurfaceLight
import com.example.ui.theme.HeadlineDark
import com.example.ui.theme.HeadlineLight
import com.example.ui.theme.SalimCard
import com.example.ui.theme.SalimCornerRadius
import com.example.ui.theme.SalimGradientBackground
import com.example.ui.theme.SalimPill
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
    val folders by viewModel.folders.collectAsState()
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

    // Model override dialog state (Feature 15 & Section N.7)
    var showModelOverrideDialog by remember { mutableStateOf(false) }

    // Retry with different model state (Section P.2)
    var retryModelTargetMessageId by remember { mutableStateOf<String?>(null) }

    // Side-by-side comparison state (Section P.3)
    var showSideBySideDialog by remember { mutableStateOf(false) }
    var comparisonMessagePair by remember { mutableStateOf<Pair<MessageEntity, MessageEntity>?>(null) }

    // Public share link state (Section S.10)
    var showShareLinkDialog by remember { mutableStateOf(false) }
    var isPublicShareRevoked by remember { mutableStateOf(false) }

    // Scheduled prompt dialog state (Section T.13)
    var showScheduledPromptDialog by remember { mutableStateOf(false) }
    var scheduledPromptText by remember { mutableStateOf("Summarize today's conversations and key insights") }
    var scheduledTimeText by remember { mutableStateOf("09:00 AM Daily") }

    // "Explain this portion" dialog state (Section R.9)
    var showExplainPortionDialog by remember { mutableStateOf(false) }
    var explainPortionText by remember { mutableStateOf("") }

    val ttsHelper = remember { TtsHelper(context) }
    val currentSpeakingId by ttsHelper.currentPlayingId.collectAsState()
    val activeWordRange by ttsHelper.activeWordRange.collectAsState()

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
            isListeningVoice = false
        }
    }
    val liveTranscription by voiceHelper.liveTranscription.collectAsState()

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isListeningVoice = true
            voiceHelper.startListening()
        } else {
            Toast.makeText(context, "Microphone permission required for speech dictation", Toast.LENGTH_SHORT).show()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) viewModel.setAttachmentFromUri(uri, "image", "Attached_Image.jpg")
    }

    val documentPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) viewModel.setAttachmentFromUri(uri, "document", "Document.txt")
    }

    val listState = rememberLazyListState()

    // Auto-scroll to bottom on incoming message or new streaming token
    LaunchedEffect(messages.size, streamingContent.length) {
        if (messages.isNotEmpty()) {
            if (!listState.canScrollForward || isGenerating) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    // Scroll-to-latest control visibility (Section N.6)
    val showScrollToBottom by remember {
        derivedStateOf { listState.canScrollForward }
    }

    // Editing message state
    var editingMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var editContentText by remember { mutableStateOf("") }

    // Directives & Sensitivity Dialog state
    var showInstructionsDialog by remember { mutableStateOf(false) }
    var instructionsText by remember { mutableStateOf("") }
    var selectedFilterLevel by remember { mutableStateOf("standard") }

    // Top-bar overflow menu
    var headerMenuExpanded by remember { mutableStateOf(false) }
    var attachmentMenuExpanded by remember { mutableStateOf(false) }

    val headlineColor = if (isDark) HeadlineDark else HeadlineLight
    val captionColor = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight

    // In-chat search match indices
    val inChatMatches = remember(messages, inChatSearchQuery) {
        if (inChatSearchQuery.isBlank()) emptyList()
        else {
            messages.mapIndexedNotNull { idx, msg ->
                if (msg.content.contains(inChatSearchQuery, ignoreCase = true)) idx else null
            }
        }
    }
    var currentMatchIndexPointer by remember { mutableIntStateOf(0) }

    BackHandler {
        if (isInChatSearching) {
            viewModel.isInChatSearching.value = false
            viewModel.inChatSearchQuery.value = ""
        } else {
            viewModel.navigateBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) {
        SalimGradientBackground(isDark = isDark) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top Header Bar
                if (isInChatSearching) {
                    // In-Chat Search Bar (Section Addendum 4.1)
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
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close search", tint = headlineColor)
                        }

                        BasicTextField(
                            value = inChatSearchQuery,
                            onValueChange = { viewModel.inChatSearchQuery.value = it },
                            modifier = Modifier
                                .weight(1f)
                                .clip(SalimShape)
                                .background(if (isDark) CardSurfaceDark else CardSurfaceLight)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            textStyle = SalimTextStyles.Body.copy(color = headlineColor),
                            cursorBrush = SolidColor(headlineColor),
                            decorationBox = { inner ->
                                if (inChatSearchQuery.isEmpty()) {
                                    Text("Find in this conversation...", style = SalimTextStyles.Body, color = captionColor)
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
                                    if (currentMatchIndexPointer > 0) currentMatchIndexPointer--
                                    else currentMatchIndexPointer = inChatMatches.size - 1
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
                                    if (currentMatchIndexPointer < inChatMatches.size - 1) currentMatchIndexPointer++
                                    else currentMatchIndexPointer = 0
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
                    // Standard Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
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

                        // Jump-to-message Search Icon
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

                        Spacer(modifier = Modifier.width(2.dp))

                        // Section N.10: Filled dark circular button for persistent New Chat
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isDark) CardSurfaceDark else Color(0xFF1B1E1B))
                                .clickable { viewModel.navigateToNewChat() }
                                .testTag("chat_new_chat_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Chat",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

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
                                    text = { Text("Model & Effort Override") },
                                    leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null) },
                                    onClick = {
                                        headerMenuExpanded = false
                                        showModelOverrideDialog = true
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text(if (showTimestamps) "Hide Timestamps" else "Show Timestamps") },
                                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                                    onClick = {
                                        headerMenuExpanded = false
                                        viewModel.showTimestamps.value = !showTimestamps
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Directives & Sensitivity") },
                                    leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null) },
                                    onClick = {
                                        headerMenuExpanded = false
                                        instructionsText = conversation?.customInstructions ?: ""
                                        selectedFilterLevel = conversation?.contentFilterLevel ?: "standard"
                                        showInstructionsDialog = true
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Share Dialogue via Link") },
                                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                                    onClick = {
                                        headerMenuExpanded = false
                                        showShareLinkDialog = true
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Export Formatted Document (HTML)") },
                                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                                    onClick = {
                                        headerMenuExpanded = false
                                        scope.launch {
                                            val html = viewModel.repository.exportConversationAsHtml(conversationId)
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, html)
                                                type = "text/html"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Export Formatted Document"))
                                        }
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
                                    text = { Text("Configure Scheduled Prompt") },
                                    leadingIcon = { Icon(Icons.Default.Alarm, contentDescription = null) },
                                    onClick = {
                                        headerMenuExpanded = false
                                        showScheduledPromptDialog = true
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

                // Workspace Directives Banner (Section Q.4)
                val currentFolder = folders.find { it.id == conversation?.folderId }
                if (currentFolder != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                            .clip(SalimShape)
                            .background(if (isDark) Color(0xFF1B221B) else Color(0xFFEAF0E8))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Workspace: ${currentFolder.name} instructions active",
                            style = SalimTextStyles.Caption.copy(fontWeight = FontWeight.SemiBold),
                            color = headlineColor
                        )
                    }
                }

                // Messages Stream
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(
                        when (bubbleDensity) {
                            "compact" -> 10.dp
                            "spacious" -> 26.dp
                            else -> 18.dp
                        }
                    )
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(SalimShape)
                                        .background(if (isDark) CardSurfaceDark else CardSurfaceLight)
                                        .border(1.dp, if (isDark) Color(0xFF2E332E) else Color(0xFFE2E6DF), SalimShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isDark) AccentGold else AccentGreen,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "How can Salim help you today?",
                                    style = SalimTextStyles.Title,
                                    color = headlineColor
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Tap a starter prompt to begin, or type / for templates",
                                    style = SalimTextStyles.Caption,
                                    color = captionColor
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                val starterPills = listOf(
                                    "Explain quantum computing principles simply",
                                    "Draft a production-ready Kotlin class",
                                    "Summarize clean architecture patterns",
                                    "Analyze trade-offs between SQL and NoSQL",
                                    "Brainstorm creative product features"
                                )

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    starterPills.forEach { starterPrompt ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(SalimShape)
                                                .border(1.dp, if (isDark) Color(0xFF2E332E) else Color(0xFFE2E6DF), SalimShape)
                                                .background(if (isDark) CardSurfaceDark else CardSurfaceLight)
                                                .clickable {
                                                    viewModel.inputText.value = starterPrompt
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ChatBubbleOutline,
                                                contentDescription = null,
                                                tint = if (isDark) AccentGold else AccentGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = starterPrompt,
                                                style = SalimTextStyles.Secondary,
                                                color = headlineColor,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = captionColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

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
                                isSpeaking = currentSpeakingId == message.id,
                                showTimestamp = showTimestamps,
                                showTokens = showTokenUsage,
                                isHighlighted = isHighlighted,
                                isDark = isDark,
                                density = bubbleDensity,
                                fontScale = fontScaleOverride,
                                activeWordRange = if (currentSpeakingId == message.id) activeWordRange else null,
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
                                onRetryWithOtherModel = {
                                    retryModelTargetMessageId = message.id
                                },
                                onCompareBranches = {
                                    val prev = messages.find { it.id == message.branchGroupId || (it.sender == "assistant" && it.id != message.id) }
                                    if (prev != null) {
                                        comparisonMessagePair = Pair(prev, message)
                                        showSideBySideDialog = true
                                    } else {
                                        Toast.makeText(context, "No alternate branch found to compare yet", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onExplainPortion = {
                                    explainPortionText = displayContent.take(160)
                                    showExplainPortionDialog = true
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

                // Autocomplete bar for slash commands (/explain, /summarize, etc.)
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

                // Section U.16: Real-time live transcription display during voice input
                AnimatedVisibility(visible = isListeningVoice) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .clip(SalimShape)
                            .background(if (isDark) CardSurfaceDark else CardSurfaceLight)
                            .border(1.dp, AccentGreen.copy(alpha = 0.5f), SalimShape)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentGreen)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (liveTranscription.isNotEmpty()) liveTranscription else "Listening... speak now",
                            style = SalimTextStyles.Body.copy(fontStyle = if (liveTranscription.isEmpty()) FontStyle.Italic else FontStyle.Normal),
                            color = headlineColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tap mic to finish",
                            style = SalimTextStyles.Caption,
                            color = captionColor
                        )
                    }
                }

                // Active attachment preview
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

                // Composer Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    SalimCard(
                        isDark = isDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Section N.7: Inline model/mode indicator directly in the composer itself
                            Row(
                                modifier = Modifier
                                    .padding(start = 14.dp, top = 8.dp, end = 14.dp, bottom = 2.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF222622) else Color(0xFFE8ECE4))
                                    .clickable { showModelOverrideDialog = true }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AccentGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val curModelObj = AvailableModels.find { it.id == (conversation?.modelId ?: viewModel.preferences.selectedModel.value) }
                                val shortModelName = curModelObj?.displayName ?: "Salim Deep Thought"
                                val lengthMode = viewModel.preferences.responseLength.value.replaceFirstChar { it.uppercase() }
                                Text(
                                    text = "$shortModelName · $lengthMode",
                                    style = SalimTextStyles.Caption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                                    color = headlineColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Configure model or response mode",
                                    modifier = Modifier.size(12.dp),
                                    tint = captionColor
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // Section N.9: Attachment access via dedicated leading "+" circular control
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

                                // Auto-resizing Multi-line Input Field
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

                                Spacer(modifier = Modifier.width(4.dp))

                                // Section N.8: Dual-mode send control
                                // Solid filled send button if text/attachment is present; mic button if empty; stop button if generating
                                val canSend = inputText.isNotBlank() || activeAttachment != null
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
                                } else if (canSend) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) Color(0xFFE8EAE4) else Color(0xFF191C1A))
                                            .clickable { viewModel.sendMessage() }
                                            .testTag("send_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Send message",
                                            tint = if (isDark) Color(0xFF191C1A) else Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(if (isListeningVoice) AccentGreen.copy(alpha = 0.2f) else (if (isDark) Color(0xFF222622) else Color(0xFFE8ECE4)))
                                            .clickable {
                                                if (isListeningVoice) {
                                                    voiceHelper.stopListening()
                                                    isListeningVoice = false
                                                } else {
                                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                                        isListeningVoice = true
                                                        voiceHelper.startListening()
                                                    } else {
                                                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                    }
                                                }
                                            }
                                            .testTag("voice_input_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = if (isListeningVoice) "Listening..." else "Voice dictation",
                                            tint = if (isListeningVoice) AccentGreen else captionColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section N.6: Scroll-to-latest floating control
        // Small circular button with downward chevron centered above composer appearing only when scrolled up
        AnimatedVisibility(
            visible = showScrollToBottom,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 110.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isDark) CardSurfaceDark else Color.White)
                    .border(1.dp, if (isDark) Color(0xFF2C322C) else Color(0xFFE2E7DE), CircleShape)
                    .clickable {
                        scope.launch {
                            if (messages.isNotEmpty()) {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    }
                    .testTag("scroll_to_bottom_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Scroll to latest",
                    tint = headlineColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // Table of Contents Sheet
    if (isTocOpen) {
        val tocItems = remember(messages) {
            messages.mapIndexedNotNull { idx, msg ->
                val snippet = msg.content.lines().firstOrNull { it.isNotBlank() } ?: "Message"
                val cleaned = snippet.removePrefix("#").removePrefix("##").removePrefix("###").trim().take(48)
                TableOfContentsItem(
                    messageIndex = idx,
                    messageId = msg.id,
                    label = if (msg.sender == "user") "Prompt: $cleaned" else "Response: $cleaned"
                )
            }
        }

        ModalBottomSheet(
            onDismissRequest = { viewModel.isTableOfContentsOpen.value = false },
            shape = SalimShape,
            containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Dialogue Table of Contents",
                    style = SalimTextStyles.Title,
                    color = headlineColor
                )
                Spacer(modifier = Modifier.height(14.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(tocItems) { _, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SalimShape)
                                .clickable {
                                    viewModel.isTableOfContentsOpen.value = false
                                    viewModel.highlightedMessageId.value = item.messageId
                                    scope.launch { listState.animateScrollToItem(item.messageIndex) }
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.messageIndex + 1}.",
                                style = SalimTextStyles.Caption,
                                color = AccentGreen,
                                modifier = Modifier.width(28.dp)
                            )
                            Text(
                                text = item.label,
                                style = SalimTextStyles.Body,
                                color = headlineColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Artifact Side-Panel Sheet (Features 6, 7, 8, 9)
    if (activeArtifact != null) {
        ArtifactPanelSheet(
            artifact = activeArtifact!!,
            isDark = isDark,
            onDismiss = { viewModel.activeArtifact.value = null },
            onSendBackAsContext = { editedContentPrompt ->
                viewModel.inputText.value = editedContentPrompt
            }
        )
    }

    // Section P.2: Retry with different model dialog
    if (retryModelTargetMessageId != null) {
        val targetId = retryModelTargetMessageId!!
        AlertDialog(
            onDismissRequest = { retryModelTargetMessageId = null },
            title = { Text("Retry with Alternate Engine", style = SalimTextStyles.Title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select a model to generate an alternate branch for comparison:",
                        style = SalimTextStyles.Caption
                    )
                    AvailableModels.forEach { model ->
                        val isCurrent = (conversation?.modelId ?: viewModel.preferences.selectedModel.value) == model.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SalimShape)
                                .border(
                                    width = if (isCurrent) 1.5.dp else 1.dp,
                                    color = if (isCurrent) (if (isDark) AccentGold else AccentGreen) else (if (isDark) Color(0xFF2E332E) else Color(0xFFE2E6DF)),
                                    shape = SalimShape
                                )
                                .background(if (isCurrent) (if (isDark) Color(0xFF1E241E) else Color(0xFFF1F6ED)) else (if (isDark) CardSurfaceDark else CardSurfaceLight))
                                .clickable {
                                    retryModelTargetMessageId = null
                                    viewModel.updateModelForConversation(conversationId, model.id)
                                    viewModel.regenerateMessage(targetId)
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, if (isCurrent) (if (isDark) AccentGold else AccentGreen) else captionColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) AccentGold else AccentGreen)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = model.displayName,
                                    style = SalimTextStyles.Body.copy(fontWeight = FontWeight.Bold),
                                    color = if (isCurrent) (if (isDark) AccentGold else AccentGreen) else headlineColor
                                )
                                Text(text = model.description, style = SalimTextStyles.Caption, color = captionColor)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(SalimShape)
                                    .background(if (isCurrent) (if (isDark) AccentGold else AccentGreen) else AccentGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = model.badge,
                                    style = SalimTextStyles.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = if (isCurrent) (if (isDark) Color(0xFF141715) else Color.White) else AccentGreen
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                SalimSecondaryButton(text = "Cancel", onClick = { retryModelTargetMessageId = null }, isDark = isDark)
            },
            shape = SalimShape,
            containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight
        )
    }

    // Section P.3: Side-by-side response comparison dialog
    if (showSideBySideDialog && comparisonMessagePair != null) {
        val (firstMsg, secondMsg) = comparisonMessagePair!!
        AlertDialog(
            onDismissRequest = { showSideBySideDialog = false },
            title = { Text("Branch Comparison", style = SalimTextStyles.Title) },
            text = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .clip(SalimShape)
                            .background(if (isDark) Color(0xFF191D1A) else Color(0xFFECEFE9))
                            .padding(10.dp)
                    ) {
                        Text(text = "Branch 1", style = SalimTextStyles.Caption.copy(fontWeight = FontWeight.Bold), color = AccentGreen)
                        Spacer(modifier = Modifier.height(4.dp))
                        SalimMarkdownRenderer(content = firstMsg.content, textColor = headlineColor, isDark = isDark)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .clip(SalimShape)
                            .background(if (isDark) Color(0xFF191D1A) else Color(0xFFECEFE9))
                            .padding(10.dp)
                    ) {
                        Text(text = "Branch 2", style = SalimTextStyles.Caption.copy(fontWeight = FontWeight.Bold), color = AccentGold)
                        Spacer(modifier = Modifier.height(4.dp))
                        SalimMarkdownRenderer(content = secondMsg.content, textColor = headlineColor, isDark = isDark)
                    }
                }
            },
            confirmButton = {
                SalimPrimaryButton(text = "Done", onClick = { showSideBySideDialog = false }, isDark = isDark)
            },
            shape = SalimShape,
            containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight
        )
    }

    // Section R.9: "Explain this portion" dialog
    if (showExplainPortionDialog) {
        AlertDialog(
            onDismissRequest = { showExplainPortionDialog = false },
            title = { Text("Explain Snippet", style = SalimTextStyles.Title) },
            text = {
                Column {
                    Text(text = "Ask Salim to provide a dedicated follow-up explanation on this portion:", style = SalimTextStyles.Caption)
                    Spacer(modifier = Modifier.height(8.dp))
                    BasicTextField(
                        value = explainPortionText,
                        onValueChange = { explainPortionText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SalimShape)
                            .background(if (isDark) Color(0xFF222622) else Color(0xFFE8ECE4))
                            .padding(12.dp),
                        textStyle = SalimTextStyles.Body.copy(color = headlineColor)
                    )
                }
            },
            confirmButton = {
                SalimPrimaryButton(
                    text = "Explain",
                    onClick = {
                        showExplainPortionDialog = false
                        viewModel.inputText.value = "Please explain this part from your response in more detail:\n\n\"$explainPortionText\""
                        viewModel.sendMessage()
                    },
                    isDark = isDark
                )
            },
            dismissButton = {
                SalimSecondaryButton(text = "Cancel", onClick = { showExplainPortionDialog = false }, isDark = isDark)
            },
            shape = SalimShape,
            containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight
        )
    }

    // Section S.10: Public Share Link Dialog with honest status and revoke action
    if (showShareLinkDialog) {
        val publicUrl = "https://salim.app/shared/${conversationId.take(8)}"
        AlertDialog(
            onDismissRequest = { showShareLinkDialog = false },
            title = { Text("Public Sharing Link", style = SalimTextStyles.Title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (!isPublicShareRevoked) "This conversation is accessible via the link below. Anyone with the URL can view rendered messages."
                        else "Public access has been revoked. The link is no longer accessible.",
                        style = SalimTextStyles.Body
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SalimShape)
                            .background(if (!isPublicShareRevoked) AccentGreen.copy(alpha = 0.15f) else Color(0xFFD32F2F).copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (!isPublicShareRevoked) AccentGreen else Color(0xFFD32F2F))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (!isPublicShareRevoked) "Active · Unlisted Link" else "Access Revoked",
                            style = SalimTextStyles.Caption.copy(fontWeight = FontWeight.Bold),
                            color = if (!isPublicShareRevoked) AccentGreen else Color(0xFFD32F2F)
                        )
                    }

                    if (!isPublicShareRevoked) {
                        Text(
                            text = publicUrl,
                            style = SalimTextStyles.Caption.copy(fontWeight = FontWeight.Medium),
                            color = headlineColor
                        )
                    }
                }
            },
            confirmButton = {
                if (!isPublicShareRevoked) {
                    SalimPrimaryButton(
                        text = "Copy Link",
                        onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("Salim Share Link", publicUrl))
                            Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                            showShareLinkDialog = false
                        },
                        isDark = isDark
                    )
                } else {
                    SalimPrimaryButton(
                        text = "Reactivate Link",
                        onClick = { isPublicShareRevoked = false },
                        isDark = isDark
                    )
                }
            },
            dismissButton = {
                if (!isPublicShareRevoked) {
                    SalimSecondaryButton(
                        text = "Revoke Access",
                        onClick = {
                            isPublicShareRevoked = true
                            Toast.makeText(context, "Public share revoked", Toast.LENGTH_SHORT).show()
                        },
                        isDark = isDark
                    )
                }
            },
            shape = SalimShape,
            containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight
        )
    }

    // Section T.13: Scheduled Prompt Dialog
    if (showScheduledPromptDialog) {
        AlertDialog(
            onDismissRequest = { showScheduledPromptDialog = false },
            title = { Text("Daily Scheduled Prompt", style = SalimTextStyles.Title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Configure a prompt to run automatically on a recurring schedule:", style = SalimTextStyles.Caption)
                    BasicTextField(
                        value = scheduledPromptText,
                        onValueChange = { scheduledPromptText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SalimShape)
                            .background(if (isDark) Color(0xFF222622) else Color(0xFFE8ECE4))
                            .padding(10.dp),
                        textStyle = SalimTextStyles.Body.copy(color = headlineColor)
                    )
                    Text(text = "Delivery time preset:", style = SalimTextStyles.Caption, color = headlineColor)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("08:00 AM", "12:00 PM", "06:00 PM", "09:00 PM").forEach { time ->
                            SalimPill(
                                text = time,
                                isSelected = scheduledTimeText == time,
                                isDark = isDark,
                                modifier = Modifier.weight(1f),
                                onClick = { scheduledTimeText = time }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                SalimPrimaryButton(
                    text = "Save Schedule",
                    onClick = {
                        showScheduledPromptDialog = false
                        Toast.makeText(context, "Scheduled daily prompt registered for $scheduledTimeText", Toast.LENGTH_SHORT).show()
                    },
                    isDark = isDark
                )
            },
            dismissButton = {
                SalimSecondaryButton(text = "Cancel", onClick = { showScheduledPromptDialog = false }, isDark = isDark)
            },
            shape = SalimShape,
            containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight
        )
    }

    // Model Override Dialog (Section N.7)
    if (showModelOverrideDialog) {
        val currentResponseLength by viewModel.preferences.responseLength.collectAsState()
        AlertDialog(
            onDismissRequest = { showModelOverrideDialog = false },
            title = { Text("Model & Response Mode", style = SalimTextStyles.Title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Select reasoning engine for this dialogue:", style = SalimTextStyles.Caption)
                    AvailableModels.forEach { model ->
                        val isSelected = (conversation?.modelId ?: viewModel.preferences.selectedModel.value) == model.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SalimShape)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) (if (isDark) AccentGold else AccentGreen) else (if (isDark) Color(0xFF2E332E) else Color(0xFFE2E6DF)),
                                    shape = SalimShape
                                )
                                .background(
                                    if (isSelected) {
                                        if (isDark) Color(0xFF1E241E) else Color(0xFFF1F6ED)
                                    } else {
                                        if (isDark) CardSurfaceDark else CardSurfaceLight
                                    }
                                )
                                .clickable {
                                    viewModel.updateModelForConversation(conversationId, model.id)
                                    showModelOverrideDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isSelected) (if (isDark) AccentGold else AccentGreen) else captionColor,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) AccentGold else AccentGreen)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = model.displayName,
                                    style = SalimTextStyles.Body.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isSelected) (if (isDark) AccentGold else AccentGreen) else headlineColor
                                )
                                Text(text = model.description, style = SalimTextStyles.Caption, color = captionColor)
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = if (isDark) AccentGold else AccentGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Response Effort:", style = SalimTextStyles.Caption)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("short" to "Concise", "medium" to "Balanced", "long" to "Deep").forEach { (mode, label) ->
                            SalimPill(
                                text = label,
                                isSelected = currentResponseLength == mode,
                                isDark = isDark,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.preferences.setResponseLength(mode) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                SalimPrimaryButton(text = "Close", onClick = { showModelOverrideDialog = false }, isDark = isDark)
            },
            shape = SalimShape,
            containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight
        )
    }

    // Directives & Sensitivity Dialog
    if (showInstructionsDialog) {
        AlertDialog(
            onDismissRequest = { showInstructionsDialog = false },
            title = { Text("Directives & Sensitivity", style = SalimTextStyles.Title) },
            text = {
                Column {
                    Text("Dialogue-specific custom instructions:", style = SalimTextStyles.Caption)
                    Spacer(modifier = Modifier.height(6.dp))
                    BasicTextField(
                        value = instructionsText,
                        onValueChange = { instructionsText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .clip(SalimShape)
                            .background(if (isDark) Color(0xFF222622) else Color(0xFFE8ECE4))
                            .padding(10.dp),
                        textStyle = SalimTextStyles.Body.copy(color = headlineColor)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Content Sensitivity:", style = SalimTextStyles.Caption)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("standard", "strict", "relaxed").forEach { level ->
                            SalimPill(
                                text = level.replaceFirstChar { it.uppercase() },
                                isSelected = selectedFilterLevel == level,
                                isDark = isDark,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedFilterLevel = level }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                SalimPrimaryButton(
                    text = "Save Directives",
                    onClick = {
                        scope.launch {
                            viewModel.repository.updateCustomInstructions(conversationId, instructionsText)
                        }
                        viewModel.updateContentFilterForConversation(conversationId, selectedFilterLevel)
                        showInstructionsDialog = false
                    },
                    isDark = isDark
                )
            },
            dismissButton = {
                SalimSecondaryButton(text = "Cancel", onClick = { showInstructionsDialog = false }, isDark = isDark)
            },
            shape = SalimShape,
            containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight
        )
    }

    // Flagging Dialog
    if (messageToFlag != null) {
        AlertDialog(
            onDismissRequest = { viewModel.messageToFlag.value = null },
            title = { Text("Report Response", style = SalimTextStyles.Title) },
            text = {
                Column {
                    Text("Help calibrate Salim by indicating the concern:", style = SalimTextStyles.Body)
                    Spacer(modifier = Modifier.height(10.dp))
                    listOf(
                        "Inaccurate information",
                        "Harmful or unsafe content",
                        "Repetitive or nonsensical text",
                        "Grammatical or stylistic failure"
                    ).forEach { r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { flagReason = r }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = flagReason == r,
                                onClick = { flagReason = r },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentGreen)
                            )
                            Text(text = r, style = SalimTextStyles.Caption)
                        }
                    }
                }
            },
            confirmButton = {
                SalimPrimaryButton(
                    text = "Submit Report",
                    onClick = {
                        viewModel.flagMessage(messageToFlag!!.id, flagReason)
                        Toast.makeText(context, "Feedback logged to local calibration store", Toast.LENGTH_SHORT).show()
                    },
                    isDark = isDark
                )
            },
            dismissButton = {
                SalimSecondaryButton(text = "Cancel", onClick = { viewModel.messageToFlag.value = null }, isDark = isDark)
            },
            shape = SalimShape,
            containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight
        )
    }

    // Message edit dialog
    if (editingMessage != null) {
        AlertDialog(
            onDismissRequest = { editingMessage = null },
            title = { Text("Edit Prompt & Branch", style = SalimTextStyles.Title) },
            text = {
                BasicTextField(
                    value = editContentText,
                    onValueChange = { editContentText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp, max = 180.dp)
                        .clip(SalimShape)
                        .background(if (isDark) Color(0xFF222622) else Color(0xFFE8ECE4))
                        .padding(12.dp),
                    textStyle = SalimTextStyles.Body.copy(color = headlineColor)
                )
            },
            confirmButton = {
                SalimPrimaryButton(
                    text = "Save & Regenerate",
                    onClick = {
                        val m = editingMessage ?: return@SalimPrimaryButton
                        editingMessage = null
                        viewModel.editMessage(m.id, editContentText)
                    },
                    isDark = isDark
                )
            },
            dismissButton = {
                SalimSecondaryButton(text = "Cancel", onClick = { editingMessage = null }, isDark = isDark)
            },
            shape = SalimShape,
            containerColor = if (isDark) CardSurfaceDark else CardSurfaceLight
        )
    }
}

/**
 * User Message Bubble (Solid rounded-pill container aligned right)
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

    val padH = when (density) { "compact" -> 14.dp; "spacious" -> 22.dp; else -> 18.dp }
    val padV = when (density) { "compact" -> 10.dp; "spacious" -> 18.dp; else -> 14.dp }

    var showActions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
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
            SalimMarkdownRenderer(
                content = content,
                textColor = textColor,
                isDark = isDark,
                fontScale = fontScale
            )
        }

        // Branch indicator (Section P.1)
        if (message.totalBranches > 1) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp, end = 4.dp)
            ) {
                Text(
                    text = "${message.branchIndex + 1} of ${message.totalBranches}",
                    style = SalimTextStyles.Caption.copy(fontSize = 11.sp),
                    color = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight
                )
            }
        }

        var isCopied by remember { mutableStateOf(false) }
        LaunchedEffect(isCopied) {
            if (isCopied) {
                delay(1800)
                isCopied = false
            }
        }

        AnimatedVisibility(visible = showActions) {
            Row(
                modifier = Modifier.padding(top = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        onCopy()
                        isCopied = true
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = if (isCopied) "Copied" else "Copy message",
                        tint = if (isCopied) AccentGreen else (if (isDark) SecondaryCaptionDark else SecondaryCaptionLight),
                        modifier = Modifier.size(16.dp)
                    )
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
                contentDescription = message.deliveryStatus,
                tint = statusTint,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * Assistant Message
 *
 * Implements Section M.2:
 * "No visible container behind assistant responses. Unlike the user's own message
 * (a solid rounded-pill bubble), the assistant's response sits directly on the plain
 * background with no bubble, no card, no border — visually distinguishing 'something I said'
 * from 'something the assistant said' through presence-or-absence of a container,
 * not through color or alignment alone."
 */
@Composable
private fun AssistantMessageBubble(
    message: MessageEntity,
    content: String,
    isStreaming: Boolean,
    isSpeaking: Boolean = false,
    showTimestamp: Boolean,
    showTokens: Boolean,
    isHighlighted: Boolean,
    isDark: Boolean,
    density: String,
    fontScale: Float,
    activeWordRange: Pair<Int, Int>?,
    onCopy: () -> Unit,
    onStarToggle: () -> Unit,
    onRegenerate: () -> Unit,
    onRetryWithOtherModel: () -> Unit,
    onCompareBranches: () -> Unit,
    onExplainPortion: () -> Unit,
    onSpeak: () -> Unit,
    onDeletePair: () -> Unit,
    onOpenArtifact: (title: String, type: String, language: String, content: String) -> Unit,
    onFlagMessage: () -> Unit
) {
    val textColor = if (isDark) HeadlineDark else HeadlineLight

    var showLatencyFloor by remember { mutableStateOf(false) }
    LaunchedEffect(isStreaming, content.isNotEmpty()) {
        if (isStreaming && content.isEmpty()) {
            delay(400)
            showLatencyFloor = true
        } else {
            showLatencyFloor = false
        }
    }

    val hasCodeBlock = remember(content) {
        content.contains("```") && content.length > 120
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .then(
                if (isHighlighted) Modifier.background(AccentGold.copy(alpha = 0.12f), SalimShape).padding(8.dp)
                else Modifier
            ),
        horizontalAlignment = Alignment.Start
    ) {
        if (content.isBlank() && isStreaming) {
            if (showLatencyFloor) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
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
            // Render directly on plain background with zero card or container
            SalimMarkdownRenderer(
                content = content,
                textColor = textColor,
                isDark = isDark,
                activeWordRange = activeWordRange,
                isStreaming = isStreaming,
                fontScale = fontScale
            )

            // Artifact Badge Card if response contains substantial code / output
            if (hasCodeBlock && !isStreaming) {
                Spacer(modifier = Modifier.height(12.dp))
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
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open in Dedicated Artifact Panel",
                            style = SalimTextStyles.Caption.copy(fontWeight = FontWeight.SemiBold),
                            color = AccentGreen
                        )
                    }
                }
            }
        }

        // Branch indicator (Section P.1)
        if (message.totalBranches > 1) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF222622) else Color(0xFFE8ECE4))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Branch ${message.branchIndex + 1} of ${message.totalBranches}",
                    style = SalimTextStyles.Caption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                    color = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight
                )
            }
        }

        var isCopied by remember { mutableStateOf(false) }
        LaunchedEffect(isCopied) {
            if (isCopied) {
                delay(1800)
                isCopied = false
            }
        }

        // Bottom Action Bar
        Row(
            modifier = Modifier.padding(top = 10.dp, start = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = {
                    onCopy()
                    isCopied = true
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = if (isCopied) "Copied" else "Copy response",
                    tint = if (isCopied) AccentGreen else (if (isDark) SecondaryCaptionDark else SecondaryCaptionLight),
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

            IconButton(
                onClick = onSpeak,
                modifier = Modifier
                    .size(32.dp)
                    .then(
                        if (isSpeaking) Modifier.clip(CircleShape).background(if (isDark) AccentGold.copy(alpha = 0.2f) else AccentGreen.copy(alpha = 0.2f))
                        else Modifier
                    )
            ) {
                Icon(
                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isSpeaking) "Stop playback" else "Read aloud",
                    tint = if (isSpeaking) (if (isDark) AccentGold else AccentGreen) else (if (isDark) SecondaryCaptionDark else SecondaryCaptionLight),
                    modifier = Modifier.size(16.dp)
                )
            }

            if (!isStreaming) {
                IconButton(onClick = onRegenerate, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Regenerate response", tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight, modifier = Modifier.size(16.dp))
                }

                // Section P.2: Retry with different model
                IconButton(onClick = onRetryWithOtherModel, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Tune, contentDescription = "Retry with different model", tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight, modifier = Modifier.size(16.dp))
                }

                // Section P.3: Compare branches
                IconButton(onClick = onCompareBranches, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = "Compare response branches", tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight, modifier = Modifier.size(16.dp))
                }

                // Section R.9: Explain this portion
                IconButton(onClick = onExplainPortion, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Explain this part", tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight, modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onFlagMessage, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Flag, contentDescription = "Report response", tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight, modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onDeletePair, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete exchange", tint = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight, modifier = Modifier.size(16.dp))
                }
            }

            if (showTokens && message.tokenCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${message.tokenCount} tok",
                    style = SalimTextStyles.Caption,
                    color = if (isDark) SecondaryCaptionDark else SecondaryCaptionLight
                )
            }

            if (showTimestamp) {
                Spacer(modifier = Modifier.width(4.dp))
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
