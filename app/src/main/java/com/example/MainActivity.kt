package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ConversationListScreen
import com.example.ui.screens.SettingsSheet
import com.example.ui.screens.StarredMessagesScreen
import com.example.ui.screens.TemplatesSheet
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val themeMode by viewModel.preferences.themeMode.collectAsState()
            val isDark = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SalimApp(viewModel = viewModel, isDark = isDark)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        // Feature 37: Share-sheet integration
        if (Intent.ACTION_SEND == intent.action && "text/plain" == intent.type) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            if (sharedText.isNotBlank()) {
                viewModel.navigateToNewChat(prefilledPrompt = sharedText)
            }
        }

        // Feature 38: Home-screen quick action shortcut
        if (intent.getBooleanExtra("action_new_chat", false)) {
            viewModel.navigateToNewChat()
        }
    }
}

@Composable
fun SalimApp(
    viewModel: MainViewModel,
    isDark: Boolean
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isSettingsOpen by viewModel.isSettingsSheetOpen.collectAsState()
    val isTemplatesOpen by viewModel.isTemplatesSheetOpen.collectAsState()

    when (val screen = currentScreen) {
        is Screen.ConversationList -> {
            ConversationListScreen(viewModel = viewModel, isDark = isDark)
        }
        is Screen.Chat -> {
            ChatScreen(
                conversationId = screen.conversationId,
                viewModel = viewModel,
                isDark = isDark
            )
        }
        is Screen.StarredMessages -> {
            StarredMessagesScreen(viewModel = viewModel, isDark = isDark)
        }
    }

    if (isSettingsOpen) {
        SettingsSheet(
            viewModel = viewModel,
            isDark = isDark,
            onDismiss = { viewModel.isSettingsSheetOpen.value = false }
        )
    }

    if (isTemplatesOpen) {
        TemplatesSheet(
            viewModel = viewModel,
            isDark = isDark,
            onDismiss = { viewModel.isTemplatesSheetOpen.value = false }
        )
    }
}
