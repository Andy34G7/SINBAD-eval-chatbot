package com.woundtriage.chat.data.llm

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * On-device LLM inference using Google MediaPipe GenAI tasks-genai.
 * Bundled model weights in assets are extracted on first launch to app storage.
 */
class MediaPipeLlmEngine(
    private val context: Context,
    private val assetModelName: String = "models/gemma_2b_it.bin"
) : TriageLlmEngine {

    private val fallbackEngine = MockTriageLlmEngine()
    private var llmInference: LlmInference? = null
    private var isInitialized = false

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext

        try {
            val destinationFile = File(context.filesDir, "llm_weights.bin")
            if (!destinationFile.exists() || destinationFile.length() == 0L) {
                // Attempt to copy from assets if bundled
                copyAssetToFile(assetModelName, destinationFile)
            }

            if (destinationFile.exists() && destinationFile.length() > 0L) {
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(destinationFile.absolutePath)
                    .setMaxTokens(512)
                    .build()
                llmInference = LlmInference.createFromOptions(context, options)
            }
        } catch (_: Exception) {
            // Weights not yet bundled or device unsupported; fallback engine is used
            llmInference = null
        } finally {
            isInitialized = true
        }
    }

    private fun copyAssetToFile(assetName: String, targetFile: File) {
        context.assets.open(assetName).use { inputStream ->
            FileOutputStream(targetFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    override fun isAvailable(): Boolean = llmInference != null

    override fun generateStream(prompt: String): Flow<String> {
        val activeLlm = llmInference ?: return fallbackEngine.generateStream(prompt)

        return flow {
            val fullResponse = withContext(Dispatchers.IO) {
                activeLlm.generateResponse(prompt) ?: ""
            }
            val words = fullResponse.split(" ")
            for ((idx, word) in words.withIndex()) {
                val token = if (idx < words.size - 1) "$word " else word
                emit(token)
                delay(15)
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        val activeLlm = llmInference ?: return@withContext fallbackEngine.generateResponse(prompt)
        try {
            activeLlm.generateResponse(prompt) ?: ""
        } catch (_: Exception) {
            fallbackEngine.generateResponse(prompt)
        }
    }
}
