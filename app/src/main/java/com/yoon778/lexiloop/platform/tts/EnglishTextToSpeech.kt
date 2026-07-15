package com.yoon778.lexiloop.platform.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.io.Closeable
import java.util.Locale
import java.util.UUID

class EnglishTextToSpeech(context: Context) : Closeable, TextToSpeech.OnInitListener {
    private val engine = TextToSpeech(context.applicationContext, this)

    @Volatile
    private var ready = false

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS &&
            engine.setLanguage(Locale.US) >= TextToSpeech.LANG_AVAILABLE
    }

    fun speak(text: String): Boolean {
        val utterance = text.trim()
        if (!ready || utterance.isEmpty()) return false
        return engine.speak(
            utterance,
            TextToSpeech.QUEUE_FLUSH,
            null,
            UUID.randomUUID().toString(),
        ) == TextToSpeech.SUCCESS
    }

    fun stop() {
        if (ready) engine.stop()
    }

    override fun close() {
        ready = false
        engine.stop()
        engine.shutdown()
    }
}
