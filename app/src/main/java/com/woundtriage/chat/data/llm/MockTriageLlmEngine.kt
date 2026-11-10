package com.woundtriage.chat.data.llm

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * High-fidelity fallback LLM engine simulating streaming token generation.
 * Used when heavy model weights are missing or during unit testing.
 */
class MockTriageLlmEngine : TriageLlmEngine {

    override fun isAvailable(): Boolean = true

    override fun generateStream(prompt: String): Flow<String> = flow {
        val simulatedTokens = parsePromptToTokens(prompt)
        for (token in simulatedTokens) {
            emit(token)
            delay(20) // Realistic token streaming rate (~50 tok/s)
        }
    }

    override suspend fun generateResponse(prompt: String): String {
        return parsePromptToTokens(prompt).joinToString("")
    }

    private fun parsePromptToTokens(prompt: String): List<String> {
        // If the prompt is for question generation or clarification:
        val words = when {
            prompt.contains("SITE", ignoreCase = true) ->
                "Could you please confirm the exact location of the wound on your foot? Is it around the toes or towards the heel?"
            prompt.contains("ISCHEMIA", ignoreCase = true) ->
                "Does your foot feel cold or pale, or do you have any muscle cramping when walking?"
            prompt.contains("NEUROPATHY", ignoreCase = true) ->
                "Do you feel numbness, tingling, or loss of feeling in the affected area?"
            prompt.contains("INFECTION", ignoreCase = true) ->
                "Is there any redness, swelling, warmth, bad odor, or drainage coming from the wound?"
            prompt.contains("AREA", ignoreCase = true) ->
                "Is the wound approximately smaller than a coin, or larger?"
            prompt.contains("DEPTH", ignoreCase = true) ->
                "Does the wound appear superficial on the skin surface, or deep into the tissue?"
            else ->
                "I understand your response and have recorded it for your physician's Case Sheet."
        }.split(" ")

        return words.mapIndexed { i, word ->
            if (i < words.size - 1) "$word " else word
        }
    }
}
