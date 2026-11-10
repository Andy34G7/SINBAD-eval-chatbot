package com.woundtriage.chat.data.llm

import kotlinx.coroutines.flow.Flow

/**
 * Interface for on-device conversational LLM inference.
 */
interface TriageLlmEngine {
    /**
     * Checks if the on-device LLM model weights are loaded and ready.
     */
    fun isAvailable(): Boolean

    /**
     * Streams generated tokens asynchronously.
     */
    fun generateStream(prompt: String): Flow<String>

    /**
     * Synchronous / single-turn generation.
     */
    suspend fun generateResponse(prompt: String): String
}
