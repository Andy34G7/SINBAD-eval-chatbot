package com.woundtriage.chat

import com.woundtriage.chat.data.classifier.HeuristicVisionClassifier
import com.woundtriage.chat.data.llm.MockTriageLlmEngine
import com.woundtriage.chat.domain.model.SinbadDimension
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VisionClassifierAndLlmTest {

    @Test
    fun testHeuristicClassifierDefaultFallback() {
        val classifier = HeuristicVisionClassifier()
        val predictions = classifier.classify(null)

        assertEquals(6, predictions.size)
        for (dim in SinbadDimension.ORDERED_DIMENSIONS) {
            val pred = predictions[dim]
            assertNotNull(pred)
            assertTrue(pred!!.confidence in 0.5f..1.0f)
            assertFalse(pred.rationale.isBlank())
        }
    }

    @Test
    fun testMockTriageLlmEngineStreaming() = runBlocking {
        val engine = MockTriageLlmEngine()
        assertTrue(engine.isAvailable())

        val tokens = engine.generateStream("Prompt asking about INFECTION dimension").toList()
        assertTrue(tokens.isNotEmpty())

        val fullResponse = tokens.joinToString("")
        assertTrue(fullResponse.contains("redness", ignoreCase = true) || fullResponse.contains("swelling", ignoreCase = true))
    }

    @Test
    fun testMockTriageLlmEngineSynchronousResponse() = runBlocking {
        val engine = MockTriageLlmEngine()
        val response = engine.generateResponse("Prompt asking about SITE")
        assertNotNull(response)
        assertTrue(response.contains("location", ignoreCase = true) || response.contains("toes", ignoreCase = true))
    }

    @Test
    fun testMockTriageLlmEnginePromptInHindi() = runBlocking {
        val engine = MockTriageLlmEngine()
        val prompt = "You are an empathetic medical triage assistant. You must converse strictly in Hindi (हिन्दी). Ask exactly 1 clear, compassionate question about diabetic foot ulcer Ischemia dimension. Do not diagnose."
        val response = engine.generateResponse(prompt)
        assertNotNull(response)
        // Should produce Hindi question regarding ischemia / cold foot
        assertTrue(response.contains("ठंडा") || response.contains("पैर"))
    }
}
