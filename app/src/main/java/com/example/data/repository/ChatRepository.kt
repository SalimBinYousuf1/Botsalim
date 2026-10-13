package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.db.ConversationDao
import com.example.data.db.ConversationEntity
import com.example.data.db.FolderDao
import com.example.data.db.FolderEntity
import com.example.data.db.MessageDao
import com.example.data.db.MessageEntity
import com.example.data.db.PromptTemplateEntity
import com.example.data.db.TemplateDao
import com.example.data.network.ChatMessagePayload
import com.example.data.network.GroqStreamingClient
import com.example.data.network.StreamEvent
import com.example.data.preferences.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val userPreferences: UserPreferences,
    private val context: Context
) {
    private val streamingClient = GroqStreamingClient()
    private val repoScope = CoroutineScope(Dispatchers.IO)

    init {
        streamingClient.prewarmConnection()
    }

    val activeConversations: Flow<List<ConversationEntity>> = conversationDao.getActiveConversations()
    val archivedConversations: Flow<List<ConversationEntity>> = conversationDao.getArchivedConversations()
    val allFolders: Flow<List<FolderEntity>> = folderDao.getAllFolders()
    val allTemplates: Flow<List<PromptTemplateEntity>> = templateDao.getAllTemplates()
    val starredMessages: Flow<List<MessageEntity>> = messageDao.getAllStarredMessages()

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
            isPinned = false
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
            isPinned = false
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

    fun stopGeneration() {
        activeStreamJob?.cancel()
        _isGenerating.value = false
        _streamingMessageId.value = null
        _streamingContent.value = ""
    }

    /**
     * Sends a user message and streams assistant response token by token.
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

        activeStreamJob = repoScope.launch {
            // Speculative local echo: insert immediately into database
            messageDao.insert(userMessage)
            messageDao.insert(assistantPlaceholder)

            conversationDao.updateDraft(conversationId, "")
            val conversation = conversationDao.getConversationById(conversationId)

            // Build system prompt based on style + custom instructions
            val systemPrompt = buildSystemPrompt(conversation?.customInstructions)
            val modelId = conversation?.modelId ?: userPreferences.selectedModel.value

            // Prepare context history (stable prefix to maximize Groq prompt caching)
            val history = messageDao.getMessagesList(conversationId)
            val payloadList = mutableListOf<ChatMessagePayload>()
            payloadList.add(ChatMessagePayload("system", systemPrompt))

            for (msg in history) {
                if (msg.id != assistantMessageId && msg.content.isNotBlank()) {
                    payloadList.add(ChatMessagePayload(msg.sender, msg.content))
                }
            }

            val style = userPreferences.responseStyle.value
            val temp = when (style) {
                "concise" -> 0.4f
                "detailed" -> 0.85f
                else -> userPreferences.temperature.value
            }
            val maxTokens = when (style) {
                "concise" -> 1500
                "detailed" -> 8192
                else -> userPreferences.maxTokens.value
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
                        // Request is confirmed sent to inference backend
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

                        // Update conversation snippet and updatedAt immediately
                        val snippet = finalContent.take(120).replace("\n", " ")
                        val conv = conversationDao.getConversationById(conversationId)
                        if (conv != null) {
                            conversationDao.update(
                                conv.copy(
                                    lastMessageSnippet = snippet,
                                    updatedAt = System.currentTimeMillis()
                                )
                            )

                            // Title generation runs fully asynchronously in background:
                            // NEVER blocks or delays the user from seeing their answer!
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
     * Regenerates the last assistant response.
     */
    fun regenerateResponse(conversationId: String, assistantMessageId: String) {
        repoScope.launch {
            val messages = messageDao.getMessagesList(conversationId)
            val assistantIdx = messages.indexOfFirst { it.id == assistantMessageId }
            if (assistantIdx > 0) {
                val userMsg = messages[assistantIdx - 1]
                if (userMsg.sender == "user") {
                    // Remove current assistant message and trigger re-send
                    messageDao.deleteById(assistantMessageId)
                    sendMessage(conversationId, userMsg.content)
                }
            }
        }
    }

    /**
     * Edits a previously sent user message, creating a branch group.
     */
    fun editMessageAndBranch(
        conversationId: String,
        userMessageId: String,
        newContent: String
    ) {
        repoScope.launch {
            val userMsg = messageDao.getMessageById(userMessageId) ?: return@launch
            val branchGroupId = userMsg.branchGroupId ?: UUID.randomUUID().toString()

            // Update original with branch group
            messageDao.update(
                userMsg.copy(
                    branchGroupId = branchGroupId,
                    totalBranches = userMsg.totalBranches + 1
                )
            )

            // Create branched new user message
            val newMsgId = UUID.randomUUID().toString()
            val newMsg = userMsg.copy(
                id = newMsgId,
                content = newContent,
                branchGroupId = branchGroupId,
                branchIndex = userMsg.branchIndex + 1,
                totalBranches = userMsg.totalBranches + 1,
                createdAt = System.currentTimeMillis()
            )
            messageDao.insert(newMsg)

            // Send from here
            sendMessage(conversationId, newContent)
        }
    }

    private fun buildSystemPrompt(conversationOverride: String?): String {
        val basePrompt = "You are Salim, a mature, highly capable, and refined conversational assistant. " +
                "You provide thoughtful, accurate, and direct responses with clarity and intellectual depth. " +
                "Format responses cleanly with markdown where appropriate."

        val styleInstructions = when (userPreferences.responseStyle.value) {
            "concise" -> "Be concise, focused, and direct. Omit conversational filler."
            "detailed" -> "Provide comprehensive, deeply elaborated explanations with extensive analysis and practical depth."
            else -> "Provide balanced, well-structured, and naturally paced responses."
        }

        val globalInstructions = userPreferences.globalInstructions.value
        val perConvInstructions = conversationOverride ?: ""

        val builder = StringBuilder(basePrompt)
        builder.append("\n\nStyle: ").append(styleInstructions)

        if (globalInstructions.isNotBlank()) {
            builder.append("\n\nUser's Global Preferences:\n").append(globalInstructions)
        }
        if (perConvInstructions.isNotBlank()) {
            builder.append("\n\nConversation Directives:\n").append(perConvInstructions)
        }

        return builder.toString()
    }

    /**
     * Extracts text content from a chosen URI (TXT, MD, CSV, etc.)
     */
    suspend fun extractTextFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext ""
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String? = reader.readLine()
            var count = 0
            while (line != null && count < 2000) { // Limit to 2000 lines
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

    /**
     * Exports a conversation as Markdown or Plain text.
     */
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

    // Folders management
    suspend fun createFolder(name: String, colorHex: String = "#537357") = withContext(Dispatchers.IO) {
        folderDao.insert(FolderEntity(id = UUID.randomUUID().toString(), name = name, colorHex = colorHex))
    }

    suspend fun deleteFolder(id: String) = withContext(Dispatchers.IO) {
        folderDao.deleteById(id)
    }

    // Templates management
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

    /**
     * Deletes ALL local data (conversations, messages, folders, templates, settings).
     */
    suspend fun deleteAllData() = withContext(Dispatchers.IO) {
        stopGeneration()
        messageDao.deleteAll()
        conversationDao.deleteAll()
        folderDao.deleteAll()
        templateDao.deleteAll()
        userPreferences.clearAllPreferences()
    }
}
