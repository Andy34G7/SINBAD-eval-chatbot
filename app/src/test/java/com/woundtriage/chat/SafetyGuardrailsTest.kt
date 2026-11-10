package com.woundtriage.chat

import com.woundtriage.chat.domain.model.Language
import com.woundtriage.chat.domain.safety.SafetyCheckResult
import com.woundtriage.chat.domain.safety.SafetyGuardrails
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyGuardrailsTest {

    @Test
    fun testEmergencyKeywordDetection() {
        // High fever
        val res1 = SafetyGuardrails.evaluate("I have a high fever of 103 and chills", Language.ENGLISH)
        assertTrue(res1 is SafetyCheckResult.Emergency)

        // Spreading red streaks
        val res2 = SafetyGuardrails.evaluate("There are red streaks spreading up my ankle", Language.ENGLISH)
        assertTrue(res2 is SafetyCheckResult.Emergency)

        // Gangrene / black skin
        val res3 = SafetyGuardrails.evaluate("The skin has turned black and smells rotting", Language.ENGLISH)
        assertTrue(res3 is SafetyCheckResult.Emergency)

        // Hindi fever keyword
        val res4 = SafetyGuardrails.evaluate("मुझे बहुत तेज बुखार और कंपकंपी है", Language.HINDI)
        assertTrue(res4 is SafetyCheckResult.Emergency)

        // Tamil black skin keyword
        val res5 = SafetyGuardrails.evaluate("என் காலில் கருப்பு தோல் உருவாகிறது", Language.TAMIL)
        assertTrue(res5 is SafetyCheckResult.Emergency)
    }

    @Test
    fun testMedicalDiagnosisDeflection() {
        val res1 = SafetyGuardrails.evaluate("Can you diagnose what disease this is?", Language.ENGLISH)
        assertTrue(res1 is SafetyCheckResult.NonDiagnosticDeflection)
        val deflection = res1 as SafetyCheckResult.NonDiagnosticDeflection
        assertTrue(deflection.responseText.contains("cannot diagnose", ignoreCase = true))

        val res2 = SafetyGuardrails.evaluate("Can you diagnose what condition is causing this sore?", Language.ENGLISH)
        assertTrue(res2 is SafetyCheckResult.NonDiagnosticDeflection)

        val res3 = SafetyGuardrails.evaluate("क्या यह कैंसर है?", Language.HINDI)
        assertTrue(res3 is SafetyCheckResult.NonDiagnosticDeflection)
    }

    @Test
    fun testPrescriptionDeflection() {
        val res1 = SafetyGuardrails.evaluate("What antibiotic pill should I take for this wound?", Language.ENGLISH)
        assertTrue(res1 is SafetyCheckResult.NonDiagnosticDeflection)
        val deflection = res1 as SafetyCheckResult.NonDiagnosticDeflection
        assertTrue(deflection.responseText.contains("cannot prescribe", ignoreCase = true))

        val res2 = SafetyGuardrails.evaluate("Can I apply turmeric or honey to cure it myself?", Language.ENGLISH)
        assertTrue(res2 is SafetyCheckResult.NonDiagnosticDeflection)

        val res3 = SafetyGuardrails.evaluate("मुझे कौन सी दवा या गोली लेनी चाहिए?", Language.HINDI)
        assertTrue(res3 is SafetyCheckResult.NonDiagnosticDeflection)
    }

    @Test
    fun testSafeDialogueInput() {
        val res = SafetyGuardrails.evaluate("It is on my right big toe and feels slightly sore.", Language.ENGLISH)
        assertTrue(res is SafetyCheckResult.Safe)
    }

    @Test
    fun testUniversalReferralNoticePresenceAcrossLanguages() {
        for (lang in Language.entries) {
            val notice = SafetyGuardrails.getUniversalReferralNotice(lang)
            assertFalse("Notice should not be empty for $lang", notice.isBlank())
        }
    }
}
