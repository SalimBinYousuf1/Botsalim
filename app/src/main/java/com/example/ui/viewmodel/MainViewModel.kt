package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.db.ConversationEntity
import com.example.data.db.FolderEntity
import com.example.data.db.MessageEntity
import com.example.data.db.PromptTemplateEntity
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object ConversationList : Screen()
    data class Chat(val conversationId: String) : Screen()
    object StarredMessages : Screen()
}

data class AttachmentDraft(
    val uri: Uri? = null,
    val type: String = "text", // "image", "document", "text"
    val name: String = "",
    val extractedText: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repository: ChatRepository = (application as SalimApplication).chatRepository
    val preferences = repository.userPreferences

    // Current navigation screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.ConversationList)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Search query in conversation list
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Folder filter: null means All
    private val _selectedFolderId = MutableStateFlow<String?>(null)
    val selectedFolderId: StateFlow<String?> = _selectedFolderId.asStateFlow()

    // Show archived list toggle
    private val _showArchived = MutableStateFlow(false)
    val showArchived: StateFlow<Boolean> = _showArchived.asStateFlow()

    // Dialog & sheet states
    val isSettingsSheetOpen = MutableStateFlow(false)
    val isTemplatesSheetOpen = MutableStateFlow(false)
    val conversationToDelete = MutableStateFlow<ConversationEntity?>(null)
    val conversationToRename = MutableStateFlow<ConversationEntity?>(null)
    val conversationForInstructions = MutableStateFlow<ConversationEntity?>(null)
    val showDeleteAllDataDialog = MutableStateFlow(false)

    // Current chat screen state
    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    // Input text in current chat
    val inputText = MutableStateFlow("")
    val activeAttachment = MutableStateFlow<AttachmentDraft?>(null)

    // Timestamps toggle in chat (Feature 17: on-demand)
    val showTimestamps = MutableStateFlow(false)

    // Filtered / Searched conversations
    @OptIn(ExperimentalCoroutinesApi::class)
    val conversationsList: StateFlow<List<ConversationEntity>> = combine(
        _searchQuery,
        _selectedFolderId,
        _showArchived
    ) { query, folderId, showArchived ->
        Triple(query, folderId, showArchived)
    }.flatMapLatest { (query, folderId, showArchived) ->
        if (showArchived) {
            repository.archivedConversations
        } else if (query.isNotBlank()) {
            repository.searchConversations(query)
        } else {
            repository.activeConversations
        }
    }.combine(_selectedFolderId) { list, folderId ->
        if (folderId != null) {
            list.filter { it.folderId == folderId }
        } else {
            list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<FolderEntity>> = repository.allFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templates: StateFlow<List<PromptTemplateEntity>> = repository.allTemplates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val starredMessages: StateFlow<List<MessageEntity>> = repository.starredMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active conversation flow
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentConversation: StateFlow<ConversationEntity?> = _activeConversationId
        .flatMapLatest { id ->
            if (id != null) repository.getConversationFlow(id) else flowOf(null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active messages flow
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentMessages: StateFlow<List<MessageEntity>> = _activeConversationId
        .flatMapLatest { id ->
            if (id != null) repository.getMessagesFlow(id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isGenerating: StateFlow<Boolean> = repository.isGenerating
    val streamingMessageId: StateFlow<String?> = repository.streamingMessageId
    val streamingContent: StateFlow<String> = repository.streamingContent

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectFolder(folderId: String?) {
        _selectedFolderId.value = folderId
    }

    fun toggleArchivedView() {
        _showArchived.value = !_showArchived.value
    }

    fun navigateToConversation(id: String) {
        // Save current draft before switching
        val oldId = _activeConversationId.value
        if (oldId != null && inputText.value.isNotBlank()) {
            viewModelScope.launch {
                repository.updateDraft(oldId, inputText.value)
            }
        }

        _activeConversationId.value = id
        _currentScreen.value = Screen.Chat(id)

        // Restore draft for the conversation
        viewModelScope.launch {
            val conv = repository.getConversationFlow(id)
            conv.collect { c ->
                if (c != null && inputText.value.isEmpty()) {
                    inputText.value = c.draftMessage
                }
            }
        }
    }

    fun navigateToNewChat(prefilledPrompt: String = "") {
        viewModelScope.launch {
            val newId = repository.createConversation()
            _activeConversationId.value = newId
            _currentScreen.value = Screen.Chat(newId)
            inputText.value = prefilledPrompt
            activeAttachment.value = null
        }
    }

    fun navigateToStarred() {
        _currentScreen.value = Screen.StarredMessages
    }

    fun navigateBack() {
        when (_currentScreen.value) {
            is Screen.Chat -> {
                // Persist draft
                val id = _activeConversationId.value
                if (id != null) {
                    viewModelScope.launch {
                        repository.updateDraft(id, inputText.value)
                    }
                }
                _activeConversationId.value = null
                _currentScreen.value = Screen.ConversationList
            }
            Screen.StarredMessages -> {
                _currentScreen.value = Screen.ConversationList
            }
            Screen.ConversationList -> {
                // At root
            }
        }
    }

    fun sendMessage() {
        val convId = _activeConversationId.value ?: return
        val text = inputText.value.trim()
        val attachment = activeAttachment.value

        if (text.isBlank() && attachment == null) return

        repository.sendMessage(
            conversationId = convId,
            prompt = text,
            attachmentUri = attachment?.uri?.toString(),
            attachmentType = attachment?.type,
            attachmentName = attachment?.name,
            extractedText = attachment?.extractedText
        )

        inputText.value = ""
        activeAttachment.value = null
    }

    fun stopGeneration() {
        repository.stopGeneration()
    }

    fun regenerateMessage(assistantMessageId: String) {
        val convId = _activeConversationId.value ?: return
        repository.regenerateResponse(convId, assistantMessageId)
    }

    fun editMessage(userMessageId: String, newContent: String) {
        val convId = _activeConversationId.value ?: return
        repository.editMessageAndBranch(convId, userMessageId, newContent)
    }

    fun toggleStarMessage(messageId: String, currentStarred: Boolean) {
        viewModelScope.launch {
            repository.toggleStarMessage(messageId, !currentStarred)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun deleteMessagePair(userMsgId: String, assistantMsgId: String) {
        val convId = _activeConversationId.value ?: return
        viewModelScope.launch {
            repository.deleteMessagePair(userMsgId, assistantMsgId)
        }
    }

    fun togglePinConversation(conversation: ConversationEntity) {
        viewModelScope.launch {
            repository.setPinned(conversation.id, !conversation.isPinned)
        }
    }

    fun toggleArchiveConversation(conversation: ConversationEntity) {
        viewModelScope.launch {
            repository.setArchived(conversation.id, !conversation.isArchived)
        }
    }

    fun duplicateConversation(conversationId: String) {
        viewModelScope.launch {
            val newId = repository.duplicateConversation(conversationId)
            if (newId.isNotEmpty()) {
                navigateToConversation(newId)
            }
        }
    }

    fun branchFromMessage(messageId: String) {
        val convId = _activeConversationId.value ?: return
        viewModelScope.launch {
            val newId = repository.branchConversation(convId, messageId)
            if (newId.isNotEmpty()) {
                navigateToConversation(newId)
            }
        }
    }

    fun renameConversation(id: String, newTitle: String) {
        viewModelScope.launch {
            repository.updateConversationTitle(id, newTitle)
        }
    }

    fun deleteConversationConfirmed() {
        val conv = conversationToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteConversation(conv.id)
            if (_activeConversationId.value == conv.id) {
                _activeConversationId.value = null
                _currentScreen.value = Screen.ConversationList
            }
            conversationToDelete.value = null
        }
    }

    fun updateFolderForConversation(conversationId: String, folderId: String?) {
        viewModelScope.launch {
            repository.updateFolder(conversationId, folderId)
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            repository.createFolder(name)
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            repository.deleteFolder(folderId)
            if (_selectedFolderId.value == folderId) {
                _selectedFolderId.value = null
            }
        }
    }

    fun insertTemplateText(content: String) {
        inputText.value = if (inputText.value.isEmpty()) content else "${inputText.value}\n$content"
        isTemplatesSheetOpen.value = false
    }

    fun createTemplate(title: String, shortcut: String, content: String) {
        viewModelScope.launch {
            repository.createTemplate(title, shortcut, content)
        }
    }

    fun deleteTemplate(id: String) {
        viewModelScope.launch {
            repository.deleteTemplate(id)
        }
    }

    fun setAttachmentFromUri(uri: Uri, type: String, name: String) {
        viewModelScope.launch {
            val extracted = if (type == "document" || type == "text") {
                repository.extractTextFromUri(uri)
            } else null

            activeAttachment.value = AttachmentDraft(
                uri = uri,
                type = type,
                name = name,
                extractedText = extracted
            )
        }
    }

    fun removeAttachment() {
        activeAttachment.value = null
    }

    fun deleteAllDataConfirmed() {
        viewModelScope.launch {
            repository.deleteAllData()
            showDeleteAllDataDialog.value = false
            _activeConversationId.value = null
            _currentScreen.value = Screen.ConversationList
        }
    }
}
