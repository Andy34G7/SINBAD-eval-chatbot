package com.woundtriage.chat.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Result produced by the vision classifier for a SINBAD dimension.
 */
data class ClassifierPrediction(
    val dimension: SinbadDimension,
    val score: Int, // 0 or 1
    val confidence: Float, // 0.0 to 1.0
    val rationale: String
)

/**
 * Result verified through the on-device conversational triage with the patient.
 */
data class PatientConfirmation(
    val dimension: SinbadDimension,
    val score: Int?, // 0 or 1 (null if pending or unverified)
    val patientRawQuote: String,
    val clinicalSummary: String,
    val verified: Boolean = false,
    val discrepancyWithClassifier: Boolean = false
)

enum class SinbadSeverity(val title: String, val level: String, val description: String) {
    LOW(
        title = "Low Risk Score",
        level = "Score 0-1",
        description = "Superficial wound with preserved sensation and intact arterial perfusion. Requires standard professional outpatient wound management."
    ),
    MODERATE(
        title = "Moderate Risk Score",
        level = "Score 2-3",
        description = "Wound shows impaired sensation, larger surface area, or local infection risk. Requires priority clinical evaluation and offloading."
    ),
    HIGH(
        title = "High Risk Score",
        level = "Score 4-6",
        description = "High risk of delayed healing, severe limb ischemia, deep structure involvement, or spreading infection. Requires urgent specialist medical attention."
    );

    companion object {
        fun fromScore(score: Int): SinbadSeverity = when {
            score <= 1 -> LOW
            score <= 3 -> MODERATE
            else -> HIGH
        }
    }
}

/**
 * Complete triage encounter assessment representing the case sheet state.
 */
data class SinbadAssessment(
    val id: String = UUID.randomUUID().toString().take(8).uppercase(),
    val timestamp: Long = System.currentTimeMillis(),
    val imagePath: String? = null,
    val patientLanguage: Language = Language.ENGLISH,
    val classifierScores: Map<SinbadDimension, ClassifierPrediction> = emptyMap(),
    val patientScores: Map<SinbadDimension, PatientConfirmation> = emptyMap(),
    val emergencyFlag: Boolean = false,
    val emergencyReason: String? = null,
    val reportedSymptoms: List<String> = emptyList(),
    val patientNotes: String = ""
) {
    /**
     * Total SINBAD score based on confirmed patient responses,
     * falling back to classifier predictions for any dimensions not directly answered.
     */
    val totalScore: Int
        get() {
            var sum = 0
            for (dim in SinbadDimension.ORDERED_DIMENSIONS) {
                val confirmed = patientScores[dim]?.score
                val predicted = classifierScores[dim]?.score ?: 0
                sum += (confirmed ?: predicted)
            }
            return sum.coerceIn(0, 6)
        }

    val severity: SinbadSeverity
        get() = SinbadSeverity.fromScore(totalScore)

    val formattedDate: String
        get() = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(timestamp))

    /**
     * Checks if all 6 dimensions have been verified by the dialogue.
     */
    val isComplete: Boolean
        get() = SinbadDimension.ORDERED_DIMENSIONS.all { patientScores[it]?.verified == true }
}
