package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.db.ConversationDao
import com.example.data.db.ConversationEntity
import com.example.data.db.FlaggedReportDao
import com.example.data.db.FlaggedReportEntity
import com.example.data.db.FolderDao
import com.example.data.db.FolderEntity
import com.example.data.db.MessageDao
import com.example.data.db.MessageEntity
import com.example.data.db.PromptTemplateEntity
import com.example.data.db.TemplateDao
import com.example.data.network.ChatMessagePayload
import com.example.data.network.GroqStreamingClient
import com.example.data.network.StreamEvent
import com.example.data.preferences.AvailableModels
import com.example.data.preferences.UserPreferences
import com.example.ui.components.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChatRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val folderDao: FolderDao,
    private val templateDao: TemplateDao,
    private val flaggedReportDao: FlaggedReportDao,
    val userPreferences: UserPreferences,
    private val context: Context
) {
    private val streamingClient = GroqStreamingClient()
    private val repoScope = CoroutineScope(Dispatchers.IO)
    private val notificationHelper = NotificationHelper(context, userPreferences)

    // Tracks currently open/viewed conversation in UI
    var activeViewingConversationId: String? = null

    init {
        streamingClient.prewarmConnection()
    }

    val activeConversations: Flow<List<ConversationEntity>> = conversationDao.getActiveConversations()
    val archivedConversations: Flow<List<ConversationEntity>> = conversationDao.getArchivedConversations()
    val allFolders: Flow<List<FolderEntity>> = folderDao.getAllFolders()
    val allTemplates: Flow<List<PromptTemplateEntity>> = templateDao.getAllTemplates()
    val starredMessages: Flow<List<MessageEntity>> = messageDao.getAllStarredMessages()
    val flaggedReports: Flow<List<FlaggedReportEntity>> = flaggedReportDao.getAllReports()

    // Streaming state for active generation
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingMessageId = MutableStateFlow<String?>(null)
    val streamingMessageId: StateFlow<String?> = _streamingMessageId.asStateFlow()

    private val _streamingContent = MutableStateFlow("")
    val streamingContent: StateFlow<String> = _streamingContent.asStateFlow()

    private var activeStreamJob: Job? = null

    fun searchConversations(query: String): Flow<List<ConversationEntity>> {
        return conversationDao.searchConversations(query)
    }

    fun getConversationFlow(id: String): Flow<ConversationEntity?> = conversationDao.getConversationFlow(id)

    fun getMessagesFlow(conversationId: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesForConversation(conversationId)

    suspend fun createConversation(
        title: String = "New conversation",
        folderId: String? = null,
        tag: String = "General"
    ): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val conv = ConversationEntity(
            id = id,
            title = title,
            folderId = folderId,
            tag = tag,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        conversationDao.insert(conv)
        id
    }

    suspend fun markConversationAsRead(id: String) = withContext(Dispatchers.IO) {
        conversationDao.updateUnreadStatus(id, false)
    }

    suspend fun updateConversationTitle(id: String, title: String) = withContext(Dispatchers.IO) {
        conversationDao.updateTitle(id, title)
    }

    suspend fun setPinned(id: String, isPinned: Boolean) = withContext(Dispatchers.IO) {
        conversationDao.setPinned(id, isPinned)
    }

    suspend fun setArchived(id: String, isArchived: Boolean) = withContext(Dispatchers.IO) {
        conversationDao.setArchived(id, isArchived)
    }

    suspend fun updateFolder(id: String, folderId: String?) = withContext(Dispatchers.IO) {
        conversationDao.updateFolder(id, folderId)
    }

    suspend fun updateCustomInstructions(id: String, instructions: String?) = withContext(Dispatchers.IO) {
        conversationDao.updateCustomInstructions(id, instructions)
    }

    suspend fun updateModelOverride(id: String, modelId: String?) = withContext(Dispatchers.IO) {
        conversationDao.updateModelId(id, modelId)
    }

    suspend fun updateContentFilterLevel(id: String, level: String) = withContext(Dispatchers.IO) {
        conversationDao.updateContentFilterLevel(id, level)
    }

    suspend fun updateDraft(id: String, draft: String) = withContext(Dispatchers.IO) {
        conversationDao.updateDraft(id, draft)
    }

    suspend fun deleteConversation(id: String) = withContext(Dispatchers.IO) {
        messageDao.deleteForConversation(id)
        conversationDao.deleteById(id)
    }

    suspend fun duplicateConversation(originalId: String): String = withContext(Dispatchers.IO) {
        val orig = conversationDao.getConversationById(originalId) ?: return@withContext ""
        val newId = UUID.randomUUID().toString()
        val newConv = orig.copy(
            id = newId,
            title = "${orig.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isPinned = false,
            hasUnreadResponse = false
        )
        conversationDao.insert(newConv)

        val messages = messageDao.getMessagesList(originalId)
        val duplicatedMessages = messages.map {
            it.copy(
                id = UUID.randomUUID().toString(),
                conversationId = newId
            )
        }
        messageDao.insertAll(duplicatedMessages)
        newId
    }

    suspend fun branchConversation(originalId: String, fromMessageId: String): String = withContext(Dispatchers.IO) {
        val orig = conversationDao.getConversationById(originalId) ?: return@withContext ""
        val newId = UUID.randomUUID().toString()
        val newConv = orig.copy(
            id = newId,
            title = "${orig.title} (Branch)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isPinned = false,
            hasUnreadResponse = false
        )
        conversationDao.insert(newConv)

        val allMessages = messageDao.getMessagesList(originalId)
        val cutoffIndex = allMessages.indexOfFirst { it.id == fromMessageId }
        val branchList = if (cutoffIndex >= 0) {
            allMessages.subList(0, cutoffIndex + 1)
        } else {
            allMessages
        }

        val copiedMessages = branchList.map {
            it.copy(id = UUID.randomUUID().toString(), conversationId = newId)
        }
        messageDao.insertAll(copiedMessages)
        newId
    }

    suspend fun toggleStarMessage(messageId: String, isStarred: Boolean) = withContext(Dispatchers.IO) {
        messageDao.setStarred(messageId, isStarred)
    }

    suspend fun deleteMessage(messageId: String) = withContext(Dispatchers.IO) {
        messageDao.deleteById(messageId)
    }

    suspend fun deleteMessagePair(userMsgId: String, assistantMsgId: String) = withContext(Dispatchers.IO) {
        messageDao.deleteById(userMsgId)
        messageDao.deleteById(assistantMsgId)
    }

    suspend fun flagMessageReport(messageId: String, conversationId: String, reason: String) = withContext(Dispatchers.IO) {
        flaggedReportDao.insert(
            FlaggedReportEntity(
                id = UUID.randomUUID().toString(),
                messageId = messageId,
                conversationId = conversationId,
                reason = reason,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    fun stopGeneration() {
        activeStreamJob?.cancel()
        _isGenerating.value = false
        _streamingMessageId.value = null
        _streamingContent.value = ""
    }

    /**
     * Sends a user message and streams assistant response token by token with background survivability.
     */
    fun sendMessage(
        conversationId: String,
        prompt: String,
        attachmentUri: String? = null,
        attachmentType: String? = null,
        attachmentName: String? = null,
        extractedText: String? = null
    ) {
        if (prompt.isBlank() && extractedText.isNullOrBlank()) return

        stopGeneration()

        val fullUserPrompt = if (!extractedText.isNullOrBlank()) {
            if (prompt.isNotBlank()) "$prompt\n\n[Attachment: $attachmentName]\n$extractedText"
            else "[Attachment: $attachmentName]\n$extractedText"
        } else {
            prompt
        }

        val userMessageId = UUID.randomUUID().toString()
        val assistantMessageId = UUID.randomUUID().toString()

        val userMessage = MessageEntity(
            id = userMessageId,
            conversationId = conversationId,
            sender = "user",
            content = fullUserPrompt,
            attachmentUri = attachmentUri,
            attachmentType = attachmentType,
            attachmentName = attachmentName,
            createdAt = System.currentTimeMillis(),
            deliveryStatus = "sending"
        )

        val assistantPlaceholder = MessageEntity(
            id = assistantMessageId,
            conversationId = conversationId,
            sender = "assistant",
            content = "",
            createdAt = System.currentTimeMillis() + 1,
            deliveryStatus = "sending"
        )

        // Survived in repository application CoroutineScope (never aborted by activity pauses)
        activeStreamJob = repoScope.launch {
            messageDao.insert(userMessage)
            messageDao.insert(assistantPlaceholder)

            conversationDao.updateDraft(conversationId, "")
            val conversation = conversationDao.getConversationById(conversationId)

            val systemPrompt = buildSystemPrompt(conversation?.customInstructions, conversation?.contentFilterLevel)
            val modelId = conversation?.modelId ?: userPreferences.selectedModel.value

            val history = messageDao.getMessagesList(conversationId)
            val payloadList = mutableListOf<ChatMessagePayload>()
            payloadList.add(ChatMessagePayload("system", systemPrompt))

            for (msg in history) {
                if (msg.id != assistantMessageId && msg.content.isNotBlank()) {
                    payloadList.add(ChatMessagePayload(msg.sender, msg.content))
                }
            }

            // Length limits based on responseLength preference
            val maxTokens = when (userPreferences.responseLength.value) {
                "short" -> 800
                "long" -> 8192
                else -> when (userPreferences.responseStyle.value) {
                    "concise" -> 1500
                    "detailed" -> 8192
                    else -> userPreferences.maxTokens.value
                }
            }

            val temp = when (userPreferences.responseStyle.value) {
                "concise" -> 0.4f
                "detailed" -> 0.85f
                else -> userPreferences.temperature.value
            }

            _isGenerating.value = true
            _streamingMessageId.value = assistantMessageId
            _streamingContent.value = ""

            val apiKey = userPreferences.getEffectiveApiKey()
            val accumulated = StringBuilder()

            streamingClient.streamChat(
                apiKey = apiKey,
                model = modelId,
                messages = payloadList,
                temperature = temp,
                topP = userPreferences.topP.value,
                maxTokens = maxTokens
            ).collect { event ->
                when (event) {
                    is StreamEvent.Connected -> {
                        messageDao.updateDeliveryStatus(userMessageId, "sent")
                        messageDao.updateDeliveryStatus(assistantMessageId, "sent")
                    }
                    is StreamEvent.Token -> {
                        accumulated.append(event.text)
                        _streamingContent.value = accumulated.toString()
                    }
                    is StreamEvent.Completed -> {
                        _isGenerating.value = false
                        _streamingMessageId.value = null
                        _streamingContent.value = ""

                        val finalContent = if (accumulated.isNotEmpty()) accumulated.toString() else event.fullText
                        val updatedAssistant = assistantPlaceholder.copy(
                            content = finalContent,
                            tokenCount = event.completionTokens,
                            deliveryStatus = "sent"
                        )
                        messageDao.update(updatedAssistant)
                        messageDao.updateDeliveryStatus(userMessageId, "sent")

                        val snippet = finalContent.take(120).replace("\n", " ")
                        val conv = conversationDao.getConversationById(conversationId)
                        if (conv != null) {
                            val isUserAway = activeViewingConversationId != conversationId
                            conversationDao.update(
                                conv.copy(
                                    lastMessageSnippet = snippet,
                                    updatedAt = System.currentTimeMillis(),
                                    hasUnreadResponse = isUserAway
                                )
                            )

                            // Unread push notification if user navigated away
                            if (isUserAway) {
                                notificationHelper.showResponseCompleted(conversationId, conv.title, snippet)
                            }

                            // Fully asynchronous non-blocking title generation
                            if (conv.title == "New conversation" || conv.title.isBlank()) {
                                repoScope.launch(Dispatchers.IO) {
                                    val generatedTitle = streamingClient.generateTitle(apiKey, prompt, finalContent)
                                    val currentConv = conversationDao.getConversationById(conversationId)
                                    if (currentConv != null && (currentConv.title == "New conversation" || currentConv.title.isBlank())) {
                                        conversationDao.updateTitle(conversationId, generatedTitle)
                                    }
                                }
                            }
                        }
                    }
                    is StreamEvent.Error -> {
                        _isGenerating.value = false
                        _streamingMessageId.value = null
                        _streamingContent.value = ""

                        val errorContent = accumulated.toString().ifEmpty {
                            "⚠️ ${event.error}"
                        }
                        messageDao.update(
                            assistantPlaceholder.copy(
                                content = errorContent,
                                deliveryStatus = "failed"
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * User-triggered action to summarize the conversation so far on demand.
     */
    fun summarizeConversation(conversationId: String) {
        repoScope.launch {
            val history = messageDao.getMessagesList(conversationId)
            if (history.isEmpty()) return@launch

            val prompt = "Please generate a clear, structured summary of our discussion so far. " +
                    "Include key topics explored, conclusions reached, and open questions or next steps."

            sendMessage(conversationId, prompt)
        }
    }

    /**
     * Calculates tokens used and percentage of model context window.
     */
    suspend fun getContextUsage(conversationId: String): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val conv = conversationDao.getConversationById(conversationId)
        val messages = messageDao.getMessagesList(conversationId)
        val totalChars = messages.sumOf { it.content.length }
        val estimatedTokens = (totalChars / 4).coerceAtLeast(1)

        val modelId = conv?.modelId ?: userPreferences.selectedModel.value
        val modelInfo = AvailableModels.find { it.id == modelId } ?: AvailableModels.first()
        Pair(estimatedTokens, modelInfo.contextWindowTokens)
    }

    fun regenerateResponse(conversationId: String, assistantMessageId: String) {
        repoScope.launch {
            val messages = messageDao.getMessagesList(conversationId)
            val assistantIdx = messages.indexOfFirst { it.id == assistantMessageId }
            if (assistantIdx > 0) {
                val userMsg = messages[assistantIdx - 1]
                if (userMsg.sender == "user") {
                    messageDao.deleteById(assistantMessageId)
                    sendMessage(conversationId, userMsg.content)
                }
            }
        }
    }

    fun editMessageAndBranch(
        conversationId: String,
        userMessageId: String,
        newContent: String
    ) {
        repoScope.launch {
            val userMsg = messageDao.getMessageById(userMessageId) ?: return@launch
            val branchGroupId = userMsg.branchGroupId ?: UUID.randomUUID().toString()

            messageDao.update(
                userMsg.copy(
                    branchGroupId = branchGroupId,
                    totalBranches = userMsg.totalBranches + 1
                )
            )

            val newMsgId = UUID.randomUUID().toString()
            val newMsg = userMsg.copy(
                id = newMsgId,
                content = newContent,
                branchGroupId = branchGroupId,
                branchIndex = userMsg.branchIndex + 1,
                totalBranches = userMsg.totalBranches + 1,
                createdAt = System.currentTimeMillis(),
                deliveryStatus = "sending"
            )
            messageDao.insert(newMsg)

            sendMessage(conversationId, newContent)
        }
    }

    private fun buildSystemPrompt(conversationOverride: String?, filterLevel: String? = "standard"): String {
        val basePrompt = "You are Salim, a mature, highly capable, and refined conversational assistant. " +
                "You provide thoughtful, accurate, and direct responses with clarity and intellectual depth. " +
                "Format responses cleanly with markdown where appropriate."

        val styleInstructions = when (userPreferences.responseStyle.value) {
            "concise" -> "Be concise, focused, and direct. Omit conversational filler."
            "detailed" -> "Provide comprehensive, deeply elaborated explanations with extensive analysis and practical depth."
            else -> "Provide balanced, well-structured, and naturally paced responses."
        }

        val lengthInstructions = when (userPreferences.responseLength.value) {
            "short" -> "Keep your answer brief, concise, and contained within 1-2 focused paragraphs."
            "long" -> "Provide a thorough, comprehensive, and exhaustive deep-dive."
            else -> ""
        }

        val safetyInstructions = when (filterLevel) {
            "relaxed" -> "Filter sensitivity is relaxed: prioritize open technical candor."
            "strict" -> "Filter sensitivity is strict: ensure utmost formal decorum."
            else -> "Filter sensitivity is standard: maintain balanced, polite engagement."
        }

        val globalInstructions = userPreferences.globalInstructions.value
        val perConvInstructions = conversationOverride ?: ""

        val builder = StringBuilder(basePrompt)
        builder.append("\n\nStyle: ").append(styleInstructions)
        if (lengthInstructions.isNotEmpty()) {
            builder.append("\nLength target: ").append(lengthInstructions)
        }
        builder.append("\nContent posture: ").append(safetyInstructions)

        if (globalInstructions.isNotBlank()) {
            builder.append("\n\nUser's Global Preferences:\n").append(globalInstructions)
        }
        if (perConvInstructions.isNotBlank()) {
            builder.append("\n\nConversation Directives:\n").append(perConvInstructions)
        }

        return builder.toString()
    }

    suspend fun extractTextFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext ""
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String? = reader.readLine()
            var count = 0
            while (line != null && count < 2000) {
                sb.append(line).append("\n")
                line = reader.readLine()
                count++
            }
            reader.close()
            sb.toString()
        } catch (e: Exception) {
            "Could not read document contents: ${e.localizedMessage}"
        }
    }

    suspend fun exportConversation(conversationId: String, asMarkdown: Boolean): String = withContext(Dispatchers.IO) {
        val conv = conversationDao.getConversationById(conversationId) ?: return@withContext ""
        val messages = messageDao.getMessagesList(conversationId)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        val sb = StringBuilder()
        if (asMarkdown) {
            sb.append("# ${conv.title}\n\n")
            sb.append("*Exported from Salim on ${dateFormat.format(Date())}*\n\n---\n\n")
            for (m in messages) {
                val role = if (m.sender == "user") "### User" else "### Salim"
                sb.append("$role *(${dateFormat.format(Date(m.createdAt))})*\n\n")
                sb.append("${m.content}\n\n---\n\n")
            }
        } else {
            sb.append("${conv.title}\n")
            sb.append("Exported from Salim on ${dateFormat.format(Date())}\n\n")
            for (m in messages) {
                val role = if (m.sender == "user") "User" else "Salim"
                sb.append("[$role - ${dateFormat.format(Date(m.createdAt))}]:\n")
                sb.append("${m.content}\n\n")
            }
        }
        sb.toString()
    }

    /**
     * Read-only conversation export as a clean, formatted document (HTML)
     * Preserving real rendered typography (Lora serif headings, Plus Jakarta Sans body, no bubble for assistant)
     */
    suspend fun exportConversationAsHtml(conversationId: String): String = withContext(Dispatchers.IO) {
        val conv = conversationDao.getConversationById(conversationId) ?: return@withContext ""
        val messages = messageDao.getMessagesList(conversationId)
        val dateFormat = SimpleDateFormat("MMMM d, yyyy · h:mm a", Locale.getDefault())

        val html = StringBuilder().apply {
            append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
            append("<meta charset=\"UTF-8\">\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
            append("<title>${conv.title} — Salim</title>\n")
            append("<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n")
            append("<link href=\"https://fonts.googleapis.com/css2?family=Lora:ital,wght@0,600;0,700;1,400&family=Plus+Jakarta+Sans:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap\" rel=\"stylesheet\">\n")
            append("<style>\n")
            append("  :root { --bg-top: #8A9A86; --bg-bottom: #F7F5EE; --headline: #181E19; --body: #252D26; --card-radius: 22px; }\n")
            append("  * { box-sizing: border-box; }\n")
            append("  body { margin: 0; padding: 40px 20px; font-family: 'Plus Jakarta Sans', sans-serif; background: linear-gradient(180deg, var(--bg-top) 0%, var(--bg-bottom) 350px, var(--bg-bottom) 100%); min-height: 100vh; color: var(--body); display: flex; justify-content: center; }\n")
            append("  .container { max-width: 720px; width: 100%; }\n")
            append("  header { margin-bottom: 40px; text-align: left; padding-bottom: 20px; border-bottom: 1px solid rgba(0,0,0,0.08); }\n")
            append("  h1.doc-title { font-family: 'Lora', serif; font-size: 32px; font-weight: 700; color: var(--headline); margin: 0 0 8px 0; }\n")
            append("  .doc-meta { font-size: 13px; color: #5D675E; }\n")
            append("  .message-wrapper { margin-bottom: 28px; display: flex; flex-direction: column; }\n")
            append("  .user-wrapper { align-items: flex-end; }\n")
            append("  .assistant-wrapper { align-items: flex-start; }\n")
            append("  .user-bubble { background: #202621; color: #FFFFFF; padding: 14px 20px; border-radius: var(--card-radius); max-width: 80%; font-size: 16px; line-height: 1.5; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }\n")
            append("  .assistant-content { background: transparent; padding: 4px 0; width: 100%; font-size: 16.5px; line-height: 1.6; color: var(--headline); }\n")
            append("  .assistant-content h1, .assistant-content h2, .assistant-content h3 { font-family: 'Lora', serif; color: var(--headline); margin-top: 24px; margin-bottom: 8px; font-weight: 700; }\n")
            append("  .assistant-content code { font-family: 'JetBrains Mono', monospace; font-size: 13.5px; background: rgba(0,0,0,0.06); padding: 2px 6px; border-radius: 6px; }\n")
            append("  .assistant-content pre { font-family: 'JetBrains Mono', monospace; font-size: 13.5px; background: #1B201C; color: #F0F4EF; padding: 16px; border-radius: var(--card-radius); overflow-x: auto; }\n")
            append("  .assistant-content pre code { background: transparent; color: inherit; padding: 0; }\n")
            append("  .assistant-content ul, .assistant-content ol { padding-left: 20px; margin: 12px 0; }\n")
            append("  .assistant-content li { margin-bottom: 8px; }\n")
            append("  .time-stamp { font-size: 11.5px; color: #7F8B80; margin-top: 6px; }\n")
            append("</style>\n</head>\n<body>\n")
            append("<div class=\"container\">\n")
            append("<header><h1 class=\"doc-title\">${conv.title}</h1><div class=\"doc-meta\">Dialogue recorded by Salim · ${dateFormat.format(Date(conv.createdAt))}</div></header>\n")
            for (m in messages) {
                val timeStr = dateFormat.format(Date(m.createdAt))
                if (m.sender == "user") {
                    append("<div class=\"message-wrapper user-wrapper\">\n")
                    append("  <div class=\"user-bubble\">${m.content.replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>")}</div>\n")
                    append("  <div class=\"time-stamp\">$timeStr</div>\n")
                    append("</div>\n")
                } else {
                    append("<div class=\"message-wrapper assistant-wrapper\">\n")
                    // Render basic markdown tags for HTML preview
                    val rendered = m.content
                        .replace("<", "&lt;").replace(">", "&gt;")
                        .replace(Regex("### (.+)"), "<h3>$1</h3>")
                        .replace(Regex("## (.+)"), "<h2>$1</h2>")
                        .replace(Regex("# (.+)"), "<h1>$1</h1>")
                        .replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")
                        .replace(Regex("\\*(.+?)\\*"), "<em>$1</em>")
                        .replace(Regex("```([a-zA-Z]*)\n([\\s\\S]*?)```"), "<pre><code>$2</code></pre>")
                        .replace(Regex("`([^`]+)`"), "<code>$1</code>")
                        .replace("\n\n", "</p><p>")
                        .replace("\n", "<br>")
                    append("  <div class=\"assistant-content\"><p>$rendered</p></div>\n")
                    append("  <div class=\"time-stamp\">Salim · $timeStr</div>\n")
                    append("</div>\n")
                }
            }
            append("</div>\n</body>\n</html>\n")
        }
        html.toString()
    }

    /**
     * Full archive JSON export (Feature 26).
     */
    suspend fun exportFullArchiveJson(): String = withContext(Dispatchers.IO) {
        val conversations = conversationDao.getAllConversationsList()
        val messages = messageDao.getAllMessagesList()
        val folders = folderDao.getAllFoldersList()
        val templates = templateDao.getAllTemplatesList()

        val root = JSONObject().apply {
            put("app", "Salim")
            put("version", 2)
            put("exportedAt", System.currentTimeMillis())

            val convsArr = JSONArray()
            for (c in conversations) {
                convsArr.put(JSONObject().apply {
                    put("id", c.id)
                    put("title", c.title)
                    put("createdAt", c.createdAt)
                    put("updatedAt", c.updatedAt)
                    put("isPinned", c.isPinned)
                    put("isArchived", c.isArchived)
                    put("folderId", c.folderId ?: "")
                    put("tag", c.tag)
                    put("customInstructions", c.customInstructions ?: "")
                    put("modelId", c.modelId ?: "")
                    put("contentFilterLevel", c.contentFilterLevel)
                })
            }
            put("conversations", convsArr)

            val msgsArr = JSONArray()
            for (m in messages) {
                msgsArr.put(JSONObject().apply {
                    put("id", m.id)
                    put("conversationId", m.conversationId)
                    put("sender", m.sender)
                    put("content", m.content)
                    put("createdAt", m.createdAt)
                    put("isStarred", m.isStarred)
                    put("branchGroupId", m.branchGroupId ?: "")
                    put("branchIndex", m.branchIndex)
                    put("totalBranches", m.totalBranches)
                    put("tokenCount", m.tokenCount)
                    put("deliveryStatus", m.deliveryStatus)
                })
            }
            put("messages", msgsArr)

            val fldrsArr = JSONArray()
            for (f in folders) {
                fldrsArr.put(JSONObject().apply {
                    put("id", f.id)
                    put("name", f.name)
                    put("colorHex", f.colorHex)
                })
            }
            put("folders", fldrsArr)

            val tmplsArr = JSONArray()
            for (t in templates) {
                tmplsArr.put(JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("shortcut", t.shortcut)
                    put("content", t.content)
                })
            }
            put("templates", tmplsArr)
        }

        root.toString(2)
    }

    /**
     * Selective / Full archive import (Feature 27).
     */
    suspend fun importArchiveJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val convsArr = root.optJSONArray("conversations") ?: JSONArray()
            val msgsArr = root.optJSONArray("messages") ?: JSONArray()
            val fldrsArr = root.optJSONArray("folders") ?: JSONArray()
            val tmplsArr = root.optJSONArray("templates") ?: JSONArray()

            val convList = mutableListOf<ConversationEntity>()
            for (i in 0 until convsArr.length()) {
                val o = convsArr.getJSONObject(i)
                convList.add(
                    ConversationEntity(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        title = o.optString("title", "Imported Dialogue"),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                        isPinned = o.optBoolean("isPinned", false),
                        isArchived = o.optBoolean("isArchived", false),
                        folderId = o.optString("folderId").ifEmpty { null },
                        tag = o.optString("tag", "General"),
                        customInstructions = o.optString("customInstructions").ifEmpty { null },
                        modelId = o.optString("modelId").ifEmpty { null },
                        contentFilterLevel = o.optString("contentFilterLevel", "standard")
                    )
                )
            }
            conversationDao.insertAll(convList)

            val msgList = mutableListOf<MessageEntity>()
            for (i in 0 until msgsArr.length()) {
                val o = msgsArr.getJSONObject(i)
                msgList.add(
                    MessageEntity(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        conversationId = o.optString("conversationId"),
                        sender = o.optString("sender", "user"),
                        content = o.optString("content", ""),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        isStarred = o.optBoolean("isStarred", false),
                        branchGroupId = o.optString("branchGroupId").ifEmpty { null },
                        branchIndex = o.optInt("branchIndex", 0),
                        totalBranches = o.optInt("totalBranches", 1),
                        tokenCount = o.optInt("tokenCount", 0),
                        deliveryStatus = o.optString("deliveryStatus", "sent")
                    )
                )
            }
            messageDao.insertAll(msgList)

            val fldrList = mutableListOf<FolderEntity>()
            for (i in 0 until fldrsArr.length()) {
                val o = fldrsArr.getJSONObject(i)
                fldrList.add(
                    FolderEntity(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        name = o.optString("name", "Imported Folder"),
                        colorHex = o.optString("colorHex", "#537357")
                    )
                )
            }
            folderDao.insertAll(fldrList)

            val tmplList = mutableListOf<PromptTemplateEntity>()
            for (i in 0 until tmplsArr.length()) {
                val o = tmplsArr.getJSONObject(i)
                tmplList.add(
                    PromptTemplateEntity(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        title = o.optString("title", "Imported Template"),
                        shortcut = o.optString("shortcut", "/shortcut"),
                        content = o.optString("content", "")
                    )
                )
            }
            templateDao.insertAll(tmplList)

            Result.success(convList.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFolder(name: String, colorHex: String = "#537357") = withContext(Dispatchers.IO) {
        folderDao.insert(FolderEntity(id = UUID.randomUUID().toString(), name = name, colorHex = colorHex))
    }

    suspend fun deleteFolder(id: String) = withContext(Dispatchers.IO) {
        folderDao.deleteById(id)
    }

    suspend fun createTemplate(title: String, shortcut: String, content: String) = withContext(Dispatchers.IO) {
        templateDao.insert(
            PromptTemplateEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                shortcut = shortcut,
                content = content
            )
        )
    }

    suspend fun deleteTemplate(id: String) = withContext(Dispatchers.IO) {
        templateDao.deleteById(id)
    }

    suspend fun deleteAllData() = withContext(Dispatchers.IO) {
        stopGeneration()
        messageDao.deleteAll()
        conversationDao.deleteAll()
        folderDao.deleteAll()
        templateDao.deleteAll()
        flaggedReportDao.deleteAll()
        userPreferences.clearAllPreferences()
    }
}
