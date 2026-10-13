package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SalimModelInfo(
    val id: String,
    val displayName: String,
    val description: String,
    val badge: String
)

val AvailableModels = listOf(
    SalimModelInfo(
        id = "openai/gpt-oss-120b",
        displayName = "Salim Deep Thought",
        description = "Flagship reasoning engine for nuanced comprehension, deep analysis, and writing",
        badge = "Flagship"
    ),
    SalimModelInfo(
        id = "openai/gpt-oss-20b",
        displayName = "Salim Fast",
        description = "Lightning-quick everyday intelligence with rapid response speed",
        badge = "Fast"
    ),
    SalimModelInfo(
        id = "qwen/qwen3.8-27b",
        displayName = "Salim Polyglot",
        description = "Multilingual mastery, mathematical accuracy, and technical precision",
        badge = "Technical"
    ),
    SalimModelInfo(
        id = "minimaxai/minimax-m2.7",
        displayName = "Salim Creative",
        description = "Expansive creative synthesis, storycraft, and expressive dialogue flow",
        badge = "Creative"
    )
)

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("salim_preferences", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "system") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _selectedModel = MutableStateFlow(
        prefs.getString(KEY_SELECTED_MODEL, "openai/gpt-oss-120b") ?: "openai/gpt-oss-120b"
    )
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _responseStyle = MutableStateFlow(
        prefs.getString(KEY_RESPONSE_STYLE, "balanced") ?: "balanced"
    )
    val responseStyle: StateFlow<String> = _responseStyle.asStateFlow()

    private val _globalInstructions = MutableStateFlow(
        prefs.getString(KEY_GLOBAL_INSTRUCTIONS, "") ?: ""
    )
    val globalInstructions: StateFlow<String> = _globalInstructions.asStateFlow()

    private val _temperature = MutableStateFlow(prefs.getFloat(KEY_TEMPERATURE, 0.7f))
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _topP = MutableStateFlow(prefs.getFloat(KEY_TOP_P, 0.95f))
    val topP: StateFlow<Float> = _topP.asStateFlow()

    private val _maxTokens = MutableStateFlow(prefs.getInt(KEY_MAX_TOKENS, 4096))
    val maxTokens: StateFlow<Int> = _maxTokens.asStateFlow()

    private val _showTokenUsage = MutableStateFlow(prefs.getBoolean(KEY_SHOW_TOKEN_USAGE, false))
    val showTokenUsage: StateFlow<Boolean> = _showTokenUsage.asStateFlow()

    private val _enterToSend = MutableStateFlow(prefs.getBoolean(KEY_ENTER_TO_SEND, false))
    val enterToSend: StateFlow<Boolean> = _enterToSend.asStateFlow()

    private val _customApiKey = MutableStateFlow(prefs.getString(KEY_CUSTOM_API_KEY, "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _cloudBackupEnabled = MutableStateFlow(prefs.getBoolean(KEY_CLOUD_BACKUP, false))
    val cloudBackupEnabled: StateFlow<Boolean> = _cloudBackupEnabled.asStateFlow()

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun setSelectedModel(modelId: String) {
        prefs.edit().putString(KEY_SELECTED_MODEL, modelId).apply()
        _selectedModel.value = modelId
    }

    fun setResponseStyle(style: String) {
        prefs.edit().putString(KEY_RESPONSE_STYLE, style).apply()
        _responseStyle.value = style
    }

    fun setGlobalInstructions(instructions: String) {
        prefs.edit().putString(KEY_GLOBAL_INSTRUCTIONS, instructions).apply()
        _globalInstructions.value = instructions
    }

    fun setTemperature(temp: Float) {
        prefs.edit().putFloat(KEY_TEMPERATURE, temp).apply()
        _temperature.value = temp
    }

    fun setTopP(topP: Float) {
        prefs.edit().putFloat(KEY_TOP_P, topP).apply()
        _topP.value = topP
    }

    fun setMaxTokens(tokens: Int) {
        prefs.edit().putInt(KEY_MAX_TOKENS, tokens).apply()
        _maxTokens.value = tokens
    }

    fun setShowTokenUsage(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_TOKEN_USAGE, show).apply()
        _showTokenUsage.value = show
    }

    fun setEnterToSend(enterToSend: Boolean) {
        prefs.edit().putBoolean(KEY_ENTER_TO_SEND, enterToSend).apply()
        _enterToSend.value = enterToSend
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key).apply()
        _customApiKey.value = key
    }

    fun setCloudBackupEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CLOUD_BACKUP, enabled).apply()
        _cloudBackupEnabled.value = enabled
    }

    /**
     * Resolves the active API key:
     * User's custom key if entered, or BuildConfig.GROQ_API_KEY if configured, or empty.
     */
    fun getEffectiveApiKey(): String {
        val custom = _customApiKey.value.trim()
        if (custom.isNotEmpty()) return custom
        return try {
            val buildKey = BuildConfig.GROQ_API_KEY
            if (buildKey.isNotEmpty() && !buildKey.startsWith("your_")) buildKey else ""
        } catch (e: Throwable) {
            ""
        }
    }

    fun clearAllPreferences() {
        prefs.edit().clear().apply()
        _themeMode.value = "system"
        _selectedModel.value = "openai/gpt-oss-120b"
        _responseStyle.value = "balanced"
        _globalInstructions.value = ""
        _temperature.value = 0.7f
        _topP.value = 0.95f
        _maxTokens.value = 4096
        _showTokenUsage.value = false
        _enterToSend.value = false
        _customApiKey.value = ""
        _cloudBackupEnabled.value = false
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SELECTED_MODEL = "selected_model"
        private const val KEY_RESPONSE_STYLE = "response_style"
        private const val KEY_GLOBAL_INSTRUCTIONS = "global_instructions"
        private const val KEY_TEMPERATURE = "temperature"
        private const val KEY_TOP_P = "top_p"
        private const val KEY_MAX_TOKENS = "max_tokens"
        private const val KEY_SHOW_TOKEN_USAGE = "show_token_usage"
        private const val KEY_ENTER_TO_SEND = "enter_to_send"
        private const val KEY_CUSTOM_API_KEY = "custom_api_key"
        private const val KEY_CLOUD_BACKUP = "cloud_backup"
    }
}
