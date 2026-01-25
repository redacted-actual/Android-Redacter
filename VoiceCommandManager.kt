package com.redactedactual.redacter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow

class VoiceCommandManager(private val context: Context) {
    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    val activeTypes = MutableStateFlow<Set<RedactionType>>(emptySet())

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let { cmd ->
                    activeTypes.value = parseCommand(cmd.lowercase())
                }
            }
            override fun onError(error: Int) { activeTypes.value = emptySet() }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
    }

    private fun parseCommand(command: String): Set<RedactionType> {
        val types = mutableSetOf<RedactionType>()
        if (command.contains("all") || command.contains("everything")) types.add(RedactionType.ALL)
        if (command.contains("email")) types.add(RedactionType.EMAIL)
        if (command.contains("phone") || command.contains("number")) types.add(RedactionType.PHONE)
        if (command.contains("ssn") || command.contains("social")) types.add(RedactionType.SSN)
        return types
    }

    fun destroy() = speechRecognizer.destroy()
}
