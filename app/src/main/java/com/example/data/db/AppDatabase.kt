package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        FolderEntity::class,
        PromptTemplateEntity::class,
        FlaggedReportEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun folderDao(): FolderDao
    abstract fun templateDao(): TemplateDao
    abstract fun flaggedReportDao(): FlaggedReportDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "salim_local_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                // Populate default starter folders
                val folderDao = database.folderDao()
                folderDao.insert(FolderEntity(id = "folder_work", name = "Work & Projects", colorHex = "#537357"))
                folderDao.insert(FolderEntity(id = "folder_ideas", name = "Creative & Ideas", colorHex = "#C89A3E"))
                folderDao.insert(FolderEntity(id = "folder_research", name = "Study & Research", colorHex = "#4A7290"))

                // Populate default prompt templates
                val templateDao = database.templateDao()
                templateDao.insert(
                    PromptTemplateEntity(
                        id = UUID.randomUUID().toString(),
                        title = "Executive Summary",
                        shortcut = "/summary",
                        content = "Please distill this into a concise executive summary with key takeaways and immediate action items."
                    )
                )
                templateDao.insert(
                    PromptTemplateEntity(
                        id = UUID.randomUUID().toString(),
                        title = "Explain Simply",
                        shortcut = "/explain",
                        content = "Explain this concept in clear, intuitive terms with a vivid analogy for someone without technical background."
                    )
                )
                templateDao.insert(
                    PromptTemplateEntity(
                        id = UUID.randomUUID().toString(),
                        title = "Constructive Critique",
                        shortcut = "/critique",
                        content = "Review this draft critically: identify blind spots, strengthen arguments, and suggest polish."
                    )
                )
                templateDao.insert(
                    PromptTemplateEntity(
                        id = UUID.randomUUID().toString(),
                        title = "Code Architecture Review",
                        shortcut = "/code",
                        content = "Analyze this code for edge cases, performance bottlenecks, readability, and idiomatic modern best practices."
                    )
                )
            }
        }
    }
}
