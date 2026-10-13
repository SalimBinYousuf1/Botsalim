package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.preferences.UserPreferences
import com.example.data.repository.ChatRepository

class SalimApplication : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var userPreferences: UserPreferences
        private set
    lateinit var chatRepository: ChatRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getDatabase(this)
        userPreferences = UserPreferences(this)
        chatRepository = ChatRepository(
            conversationDao = database.conversationDao(),
            messageDao = database.messageDao(),
            folderDao = database.folderDao(),
            templateDao = database.templateDao(),
            userPreferences = userPreferences,
            context = this
        )
    }

    companion object {
        lateinit var instance: SalimApplication
            private set
    }
}
