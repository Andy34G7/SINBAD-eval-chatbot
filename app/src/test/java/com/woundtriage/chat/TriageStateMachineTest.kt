package com.woundtriage.chat

import com.woundtriage.chat.domain.model.ClassifierPrediction
import com.woundtriage.chat.domain.model.Language
import com.woundtriage.chat.domain.model.MessageSender
import com.woundtriage.chat.domain.model.SinbadAssessment
import com.woundtriage.chat.domain.model.SinbadDimension
import com.woundtriage.chat.domain.model.SinbadSeverity
import com.woundtriage.chat.domain.statemachine.TriageState
import com.woundtriage.chat.domain.statemachine.TriageStateMachine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TriageStateMachineTest {

    @Test
    fun testStateMachineProgressionThroughAllSixDimensions() {
        val stateMachine = TriageStateMachine(language = Language.ENGLISH)
        assertEquals(TriageState.AwaitingImage, stateMachine.state)

        // Provide initial classifier baseline predictions
        val baseline = SinbadDimension.ORDERED_DIMENSIONS.associateWith { dim ->
            ClassifierPrediction(dim, score = 0, confidence = 0.8f, rationale = "Baseline normal")
        }

        val initialMessages = stateMachine.startWithClassifierBaseline(
            imagePath = "/data/test/wound.jpg",
            baselinePredictions = baseline
        )

        assertEquals(2, initialMessages.size) // Greeting + first question (SITE)
        assertTrue(stateMachine.state is TriageState.Questioning)
        assertEquals(SinbadDimension.SITE, (stateMachine.state as TriageState.Questioning).currentDimension)

        // 1. Answer SITE: Toes (0 pts)
        val repliesSite = stateMachine.processUserMessage("The wound is on my big toe.")
        assertNotNull(repliesSite)
        assertEquals(SinbadDimension.ISCHEMIA, (stateMachine.state as TriageState.Questioning).currentDimension)

        // 2. Answer ISCHEMIA: Cold foot, leg cramps (1 pt)
        val repliesIschemia = stateMachine.processUserMessage("Yes, my foot is very cold and my calf cramps when walking.")
        assertNotNull(repliesIschemia)
        assertEquals(SinbadDimension.NEUROPATHY, (stateMachine.state as TriageState.Questioning).currentDimension)

        // 3. Answer NEUROPATHY: Tingling and numbness (1 pt)
        val repliesNeuropathy = stateMachine.processUserMessage("Yes, I have numbness and pins and needles.")
        assertNotNull(repliesNeuropathy)
        assertEquals(SinbadDimension.BACTERIAL_INFECTION, (stateMachine.state as TriageState.Questioning).currentDimension)

        // 4. Answer BACTERIAL_INFECTION: Pus and redness (1 pt)
        val repliesInfection = stateMachine.processUserMessage("There is thick pus draining and bad smell.")
        assertNotNull(repliesInfection)
        assertEquals(SinbadDimension.AREA, (stateMachine.state as TriageState.Questioning).currentDimension)

        // 5. Answer AREA: Smaller than a coin (0 pts)
        val repliesArea = stateMachine.processUserMessage("It is quite small, smaller than a coin.")
        assertNotNull(repliesArea)
        assertEquals(SinbadDimension.DEPTH, (stateMachine.state as TriageState.Questioning).currentDimension)

        // 6. Answer DEPTH: Deep wound (1 pt)
        val repliesDepth = stateMachine.processUserMessage("It looks very deep into the flesh.")
        assertNotNull(repliesDepth)

        // State Machine should now be Completed!
        assertTrue(stateMachine.state is TriageState.Completed)

        val assessment = stateMachine.getCurrentAssessment()
        assertTrue(assessment.isComplete)

        // Expected score: Site(0) + Ischemia(1) + Neuropathy(1) + Infection(1) + Area(0) + Depth(1) = 4
        assertEquals(4, assessment.totalScore)
        assertEquals(SinbadSeverity.HIGH, assessment.severity)

        // Verify discrepancy detection:
        // Classifier baseline had score 0 for Ischemia, but patient confirmed 1
        val ischemiaConfirmation = assessment.patientScores[SinbadDimension.ISCHEMIA]
        assertNotNull(ischemiaConfirmation)
        assertTrue(ischemiaConfirmation!!.discrepancyWithClassifier)

        // Verify reported symptoms populated
        assertTrue(assessment.reportedSymptoms.isNotEmpty())
    }

    @Test
    fun testEmergencyHandlingAndSpecializedAcknowledgment() {
        val stateMachine = TriageStateMachine(language = Language.ENGLISH)
        val baseline = SinbadDimension.ORDERED_DIMENSIONS.associateWith { dim ->
            ClassifierPrediction(dim, score = 0, confidence = 0.8f, rationale = "Baseline normal")
        }
        stateMachine.startWithClassifierBaseline("/path/wound.jpg", baseline)

        // User responds with emergency symptoms (high fever)
        val replies = stateMachine.processUserMessage("The wound is on my toe, but I have a high fever of 103 and chills.")

        // Should include an emergency message
        assertTrue(replies.any { it.sender == MessageSender.EMERGENCY })

        // Assessment should have emergency flag set
        val assessment = stateMachine.getCurrentAssessment()
        assertTrue(assessment.emergencyFlag)
        assertNotNull(assessment.emergencyReason)

        // Assistant question should contain emergency-aware cautionary acknowledgment
        val assistantReply = replies.firstOrNull { it.sender == MessageSender.ASSISTANT }
        assertNotNull(assistantReply)
        assertTrue(assistantReply!!.text.contains("NOTE:", ignoreCase = true) || assistantReply.text.contains("urgent", ignoreCase = true))
    }

    @Test
    fun testLlmPromptConstruction() {
        val stateMachine = TriageStateMachine(language = Language.HINDI)
        val prompt = stateMachine.constructLlmPrompt(SinbadDimension.ISCHEMIA, "Arterial perfusion unverified")

        assertTrue(prompt.contains("Hindi", ignoreCase = true) || prompt.contains("हिन्दी"))
        assertTrue(prompt.contains("Ischemia", ignoreCase = true))
        assertTrue(prompt.contains("Do not diagnose", ignoreCase = true))
    }

    @Test
    fun testLowSeverityScoreCalculation() {
        val assessment = SinbadAssessment(
            classifierScores = SinbadDimension.ORDERED_DIMENSIONS.associateWith {
                ClassifierPrediction(it, 0, 0.9f, "Normal")
            }
        )
        assertEquals(0, assessment.totalScore)
        assertEquals(SinbadSeverity.LOW, assessment.severity)
    }

    @Test
    fun testModerateSeverityScoreCalculation() {
        val stateMachine = TriageStateMachine(language = Language.HINDI)
        val baseline = SinbadDimension.ORDERED_DIMENSIONS.associateWith { dim ->
            ClassifierPrediction(dim, score = 0, confidence = 0.8f, rationale = "Baseline")
        }
        stateMachine.startWithClassifierBaseline("/path/wound.jpg", baseline)

        // Answer Site (उंगली -> 0), Ischemia (हाँ ठंडा -> 1), Neuropathy (हाँ सुन्न -> 1), Infection (नहीं -> 0), Area (छोटा -> 0), Depth (सतही -> 0)
        stateMachine.processUserMessage("पैर की उंगली पर है")
        stateMachine.processUserMessage("हाँ ठंडा रहता है")
        stateMachine.processUserMessage("हाँ सुन्न महसूस होता है")
        stateMachine.processUserMessage("नहीं कोई मवाद नहीं है")
        stateMachine.processUserMessage("बहुत छोटा है")
        stateMachine.processUserMessage("सतही है")

        val assessment = stateMachine.getCurrentAssessment()
        assertEquals(2, assessment.totalScore)
        assertEquals(SinbadSeverity.MODERATE, assessment.severity)
    }
}
