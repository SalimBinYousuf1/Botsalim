package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceInputHelper(
    private val context: Context,
    private val onFinalResult: (String) -> Unit
) {
    private var recognizer: SpeechRecognizer? = null
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    // Live partial transcription stream (Section U.16)
    private val _liveTranscription = MutableStateFlow("")
    val liveTranscription: StateFlow<String> = _liveTranscription.asStateFlow()

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, "Speech recognition is unavailable on this device", Toast.LENGTH_SHORT).show()
            return
        }

        stopListening()
        _liveTranscription.value = ""

        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _isListening.value = false
                }
                override fun onError(error: Int) {
                    _isListening.value = false
                    _liveTranscription.value = ""
                }
                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        _liveTranscription.value = text
                        onFinalResult(text)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        _liveTranscription.value = matches[0]
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        try {
            recognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            _liveTranscription.value = ""
        }
    }

    fun stopListening() {
        try {
            recognizer?.stopListening()
            recognizer?.destroy()
        } catch (e: Exception) {
            // Ignored
        } finally {
            recognizer = null
            _isListening.value = false
        }
    }
}

class TtsHelper(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlayingId = MutableStateFlow<String?>(null)
    val currentPlayingId: StateFlow<String?> = _currentPlayingId.asStateFlow()

    // Word-level highlight range: (startIndex, endIndex) within the message text
    private val _activeWordRange = MutableStateFlow<Pair<Int, Int>?>(null)
    val activeWordRange: StateFlow<Pair<Int, Int>?> = _activeWordRange.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isPlaying.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isPlaying.value = false
                    _currentPlayingId.value = null
                    _activeWordRange.value = null
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isPlaying.value = false
                    _currentPlayingId.value = null
                    _activeWordRange.value = null
                }

                override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    super.onRangeStart(utteranceId, start, end, frame)
                    _activeWordRange.value = Pair(start, end)
                }
            })
            isInitialized = true
        }
    }

    fun speak(id: String, text: String) {
        if (!isInitialized || text.isBlank()) return

        if (_currentPlayingId.value == id && _isPlaying.value) {
            stop()
            return
        }

        stop()
        _currentPlayingId.value = id
        _isPlaying.value = true
        _activeWordRange.value = null

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id)
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, id)
    }

    fun stop() {
        tts?.stop()
        _currentPlayingId.value = null
        _isPlaying.value = false
        _activeWordRange.value = null
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
