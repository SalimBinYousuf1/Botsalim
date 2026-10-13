package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getActiveConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    suspend fun getAllConversationsList(): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE id = :id")
    fun getConversationFlow(id: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Query("""
        SELECT DISTINCT c.* FROM conversations c
        LEFT JOIN messages m ON c.id = m.conversationId
        WHERE c.isArchived = 0 AND (
            c.title LIKE '%' || :query || '%' 
            OR m.content LIKE '%' || :query || '%'
        )
        ORDER BY c.isPinned DESC, c.updatedAt DESC
    """)
    fun searchConversations(query: String): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(conversations: List<ConversationEntity>)

    @Update
    suspend fun update(conversation: ConversationEntity)

    @Query("UPDATE conversations SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: String, isPinned: Boolean)

    @Query("UPDATE conversations SET isArchived = :isArchived WHERE id = :id")
    suspend fun setArchived(id: String, isArchived: Boolean)

    @Query("UPDATE conversations SET hasUnreadResponse = :hasUnread WHERE id = :id")
    suspend fun updateUnreadStatus(id: String, hasUnread: Boolean)

    @Query("UPDATE conversations SET modelId = :modelId WHERE id = :id")
    suspend fun updateModelId(id: String, modelId: String?)

    @Query("UPDATE conversations SET contentFilterLevel = :level WHERE id = :id")
    suspend fun updateContentFilterLevel(id: String, level: String)

    @Query("UPDATE conversations SET title = :title, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTitle(id: String, title: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE conversations SET draftMessage = :draft WHERE id = :id")
    suspend fun updateDraft(id: String, draft: String)

    @Query("UPDATE conversations SET folderId = :folderId WHERE id = :id")
    suspend fun updateFolder(id: String, folderId: String?)

    @Query("UPDATE conversations SET customInstructions = :instructions WHERE id = :id")
    suspend fun updateCustomInstructions(id: String, instructions: String?)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM conversations")
    suspend fun deleteAll()
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    suspend fun getMessagesList(conversationId: String): List<MessageEntity>

    @Query("SELECT * FROM messages ORDER BY createdAt ASC")
    suspend fun getAllMessagesList(): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE isStarred = 1 ORDER BY createdAt DESC")
    fun getAllStarredMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE branchGroupId = :groupId ORDER BY branchIndex ASC")
    fun getBranchMessages(groupId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Update
    suspend fun update(message: MessageEntity)

    @Query("UPDATE messages SET isStarred = :isStarred WHERE id = :id")
    suspend fun setStarred(id: String, isStarred: Boolean)

    @Query("UPDATE messages SET deliveryStatus = :status WHERE id = :id")
    suspend fun updateDeliveryStatus(id: String, status: String)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteForConversation(conversationId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders ORDER BY name ASC")
    suspend fun getAllFoldersList(): List<FolderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(folders: List<FolderEntity>)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM folders")
    suspend fun deleteAll()
}

@Dao
interface TemplateDao {
    @Query("SELECT * FROM prompt_templates ORDER BY title ASC")
    fun getAllTemplates(): Flow<List<PromptTemplateEntity>>

    @Query("SELECT * FROM prompt_templates ORDER BY title ASC")
    suspend fun getAllTemplatesList(): List<PromptTemplateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: PromptTemplateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<PromptTemplateEntity>)

    @Query("DELETE FROM prompt_templates WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM prompt_templates")
    suspend fun deleteAll()
}

@Dao
interface FlaggedReportDao {
    @Query("SELECT * FROM flagged_reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<FlaggedReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: FlaggedReportEntity)

    @Query("DELETE FROM flagged_reports")
    suspend fun deleteAll()
}
