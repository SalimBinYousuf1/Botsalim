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
    val badge: String,
    val contextWindowTokens: Int = 32768
)

val AvailableModels = listOf(
    SalimModelInfo(
        id = "openai/gpt-oss-120b",
        displayName = "Salim Deep Thought",
        description = "Flagship reasoning engine for nuanced comprehension, deep analysis, and writing",
        badge = "Flagship",
        contextWindowTokens = 131072
    ),
    SalimModelInfo(
        id = "openai/gpt-oss-20b",
        displayName = "Salim Fast",
        description = "Lightning-quick everyday intelligence with rapid response speed",
        badge = "Fast",
        contextWindowTokens = 32768
    ),
    SalimModelInfo(
        id = "qwen/qwen3.8-27b",
        displayName = "Salim Polyglot",
        description = "Multilingual mastery, mathematical accuracy, and technical precision",
        badge = "Technical",
        contextWindowTokens = 32768
    ),
    SalimModelInfo(
        id = "minimaxai/minimax-m2.7",
        displayName = "Salim Creative",
        description = "Expansive creative synthesis, storycraft, and expressive dialogue flow",
        badge = "Creative",
        contextWindowTokens = 131072
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

    private val _responseLength = MutableStateFlow(
        prefs.getString(KEY_RESPONSE_LENGTH, "medium") ?: "medium"
    )
    val responseLength: StateFlow<String> = _responseLength.asStateFlow()

    private val _contextTrimmingStrategy = MutableStateFlow(
        prefs.getString(KEY_CONTEXT_TRIMMING, "warn") ?: "warn"
    )
    val contextTrimmingStrategy: StateFlow<String> = _contextTrimmingStrategy.asStateFlow()

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

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _quietHoursEnabled = MutableStateFlow(prefs.getBoolean(KEY_QUIET_HOURS_ENABLED, false))
    val quietHoursEnabled: StateFlow<Boolean> = _quietHoursEnabled.asStateFlow()

    private val _quietHoursStart = MutableStateFlow(prefs.getString(KEY_QUIET_HOURS_START, "22:00") ?: "22:00")
    val quietHoursStart: StateFlow<String> = _quietHoursStart.asStateFlow()

    private val _quietHoursEnd = MutableStateFlow(prefs.getString(KEY_QUIET_HOURS_END, "07:00") ?: "07:00")
    val quietHoursEnd: StateFlow<String> = _quietHoursEnd.asStateFlow()

    private val _reducedMotion = MutableStateFlow(prefs.getBoolean(KEY_REDUCED_MOTION, false))
    val reducedMotion: StateFlow<Boolean> = _reducedMotion.asStateFlow()

    private val _bubbleDensity = MutableStateFlow(prefs.getString(KEY_BUBBLE_DENSITY, "comfortable") ?: "comfortable")
    val bubbleDensity: StateFlow<String> = _bubbleDensity.asStateFlow()

    private val _fontScaleOverride = MutableStateFlow(prefs.getFloat(KEY_FONT_SCALE_OVERRIDE, 1.0f))
    val fontScaleOverride: StateFlow<Float> = _fontScaleOverride.asStateFlow()

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

    fun setResponseLength(length: String) {
        prefs.edit().putString(KEY_RESPONSE_LENGTH, length).apply()
        _responseLength.value = length
    }

    fun setContextTrimmingStrategy(strategy: String) {
        prefs.edit().putString(KEY_CONTEXT_TRIMMING, strategy).apply()
        _contextTrimmingStrategy.value = strategy
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

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        _notificationsEnabled.value = enabled
    }

    fun setQuietHoursEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QUIET_HOURS_ENABLED, enabled).apply()
        _quietHoursEnabled.value = enabled
    }

    fun setQuietHoursTimes(start: String, end: String) {
        prefs.edit().putString(KEY_QUIET_HOURS_START, start).putString(KEY_QUIET_HOURS_END, end).apply()
        _quietHoursStart.value = start
        _quietHoursEnd.value = end
    }

    fun setReducedMotion(reduced: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCED_MOTION, reduced).apply()
        _reducedMotion.value = reduced
    }

    fun setBubbleDensity(density: String) {
        prefs.edit().putString(KEY_BUBBLE_DENSITY, density).apply()
        _bubbleDensity.value = density
    }

    fun setFontScaleOverride(scale: Float) {
        prefs.edit().putFloat(KEY_FONT_SCALE_OVERRIDE, scale).apply()
        _fontScaleOverride.value = scale
    }

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
        _responseLength.value = "medium"
        _contextTrimmingStrategy.value = "warn"
        _globalInstructions.value = ""
        _temperature.value = 0.7f
        _topP.value = 0.95f
        _maxTokens.value = 4096
        _showTokenUsage.value = false
        _enterToSend.value = false
        _customApiKey.value = ""
        _cloudBackupEnabled.value = false
        _notificationsEnabled.value = true
        _quietHoursEnabled.value = false
        _quietHoursStart.value = "22:00"
        _quietHoursEnd.value = "07:00"
        _reducedMotion.value = false
        _bubbleDensity.value = "comfortable"
        _fontScaleOverride.value = 1.0f
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SELECTED_MODEL = "selected_model"
        private const val KEY_RESPONSE_STYLE = "response_style"
        private const val KEY_RESPONSE_LENGTH = "response_length"
        private const val KEY_CONTEXT_TRIMMING = "context_trimming"
        private const val KEY_GLOBAL_INSTRUCTIONS = "global_instructions"
        private const val KEY_TEMPERATURE = "temperature"
        private const val KEY_TOP_P = "top_p"
        private const val KEY_MAX_TOKENS = "max_tokens"
        private const val KEY_SHOW_TOKEN_USAGE = "show_token_usage"
        private const val KEY_ENTER_TO_SEND = "enter_to_send"
        private const val KEY_CUSTOM_API_KEY = "custom_api_key"
        private const val KEY_CLOUD_BACKUP = "cloud_backup"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_QUIET_HOURS_ENABLED = "quiet_hours_enabled"
        private const val KEY_QUIET_HOURS_START = "quiet_hours_start"
        private const val KEY_QUIET_HOURS_END = "quiet_hours_end"
        private const val KEY_REDUCED_MOTION = "reduced_motion"
        private const val KEY_BUBBLE_DENSITY = "bubble_density"
        private const val KEY_FONT_SCALE_OVERRIDE = "font_scale_override"
    }
}
