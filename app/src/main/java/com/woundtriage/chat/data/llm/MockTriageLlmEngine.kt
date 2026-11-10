package com.woundtriage.chat.data.llm

import com.woundtriage.chat.domain.model.Language
import com.woundtriage.chat.domain.model.SinbadDimension
import com.woundtriage.chat.domain.translation.IndicTranslations
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * High-fidelity fallback LLM engine simulating streaming token generation.
 * Handles both structured LLM triage prompt generation and raw text streaming.
 */
class MockTriageLlmEngine : TriageLlmEngine {

    override fun isAvailable(): Boolean = true

    override fun generateStream(prompt: String): Flow<String> = flow {
        val simulatedTokens = parsePromptToTokens(prompt)
        for (token in simulatedTokens) {
            emit(token)
            delay(18) // Realistic token streaming rate (~50 tok/s)
        }
    }

    override suspend fun generateResponse(prompt: String): String {
        return parsePromptToTokens(prompt).joinToString("")
    }

    private fun parsePromptToTokens(prompt: String): List<String> {
        // 1. If prompt is an LLM directive asking for a question in a specific language
        if (prompt.contains("empathetic medical triage assistant", ignoreCase = true)) {
            val detectedDim = SinbadDimension.ORDERED_DIMENSIONS.firstOrNull {
                prompt.contains(it.title, ignoreCase = true) || prompt.contains(it.name, ignoreCase = true)
            } ?: SinbadDimension.SITE

            val detectedLang = Language.entries.firstOrNull {
                prompt.contains(it.englishName, ignoreCase = true) || prompt.contains(it.nativeName, ignoreCase = true)
            } ?: Language.ENGLISH

            val question = IndicTranslations.getQuestion(detectedDim, detectedLang)
            return splitToTokens(question)
        }

        // 2. If prompt contains dimension keywords directly
        val generatedText = when {
            prompt.contains("ISCHEMIA", ignoreCase = true) ->
                "Does your foot often feel noticeably cold to touch, or do you experience cramping or pain in your calf muscles when walking?"
            prompt.contains("NEUROPATHY", ignoreCase = true) ->
                "In that foot, do you experience any numbness, burning, pins-and-needles, or reduced ability to feel sensations when touched?"
            prompt.contains("INFECTION", ignoreCase = true) || prompt.contains("BACTERIAL", ignoreCase = true) ->
                "Do you notice swelling, significant warmth, expanding redness, a bad odor, or any pus or fluid oozing from the wound?"
            prompt.contains("AREA", ignoreCase = true) ->
                "Roughly how large is the open wound? Is it smaller than a standard coin (fingertip size / under 1 cm²), or is it larger?"
            prompt.contains("DEPTH", ignoreCase = true) ->
                "Is this a surface-level skin sore, or does it feel deep into the flesh, potentially exposing tendon or bone?"
            prompt.contains("SITE", ignoreCase = true) ->
                "Where exactly is this wound situated on your foot? Is it on your toes or front of the foot, or is it on the arch, sole, or heel?"
            else -> prompt
        }

        return splitToTokens(generatedText)
    }

    private fun splitToTokens(text: String): List<String> {
        val words = text.split(" ")
        return words.mapIndexed { i, word ->
            if (i < words.size - 1) "$word " else word
        }
    }
}
