package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    indices = [
        Index(value = ["updatedAt"]),
        Index(value = ["isPinned"]),
        Index(value = ["isArchived"])
    ]
)
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val folderId: String? = null,
    val tag: String = "General",
    val customInstructions: String? = null,
    val modelId: String? = null,
    val draftMessage: String = "",
    val lastMessageSnippet: String = ""
)

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["createdAt"]),
        Index(value = ["isStarred"]),
        Index(value = ["branchGroupId"])
    ]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val sender: String, // "user" or "assistant"
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isStarred: Boolean = false,
    val branchGroupId: String? = null,
    val branchIndex: Int = 0,
    val totalBranches: Int = 1,
    val tokenCount: Int = 0,
    val attachmentUri: String? = null,
    val attachmentType: String? = null, // "image", "document", "text"
    val attachmentName: String? = null,
    val deliveryStatus: String = "sent" // "sending", "sent", "failed"
)

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String = "#537357",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "prompt_templates")
data class PromptTemplateEntity(
    @PrimaryKey val id: String,
    val title: String,
    val shortcut: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)
