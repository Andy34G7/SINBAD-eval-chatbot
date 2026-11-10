package com.woundtriage.chat.domain.statemachine

import com.woundtriage.chat.domain.model.ChatMessage
import com.woundtriage.chat.domain.model.ClassifierPrediction
import com.woundtriage.chat.domain.model.Language
import com.woundtriage.chat.domain.model.MessageSender
import com.woundtriage.chat.domain.model.PatientConfirmation
import com.woundtriage.chat.domain.model.SinbadAssessment
import com.woundtriage.chat.domain.model.SinbadDimension
import com.woundtriage.chat.domain.safety.SafetyCheckResult
import com.woundtriage.chat.domain.safety.SafetyGuardrails
import com.woundtriage.chat.domain.translation.IndicTranslations

sealed class TriageState {
    data object AwaitingImage : TriageState()
    data class Questioning(
        val currentDimension: SinbadDimension,
        val dimensionIndex: Int,
        val totalDimensions: Int = SinbadDimension.ORDERED_DIMENSIONS.size
    ) : TriageState()
    data class Completed(val assessment: SinbadAssessment) : TriageState()
}

/**
 * Hybrid State Machine orchestrating the 6 SINBAD clinical dimensions,
 * handling patient dialogue, safety verification, and comparative scoring.
 */
class TriageStateMachine(
    private val language: Language = Language.ENGLISH,
    val initialAssessment: SinbadAssessment = SinbadAssessment(patientLanguage = language)
) {
    private var currentDimensionIndex = 0
    private var assessment = initialAssessment

    var state: TriageState = TriageState.AwaitingImage
        private set

    /**
     * Initializes the triage sequence with preliminary vision classifier baseline predictions.
     */
    fun startWithClassifierBaseline(
        imagePath: String?,
        baselinePredictions: Map<SinbadDimension, ClassifierPrediction>
    ): List<ChatMessage> {
        assessment = assessment.copy(
            imagePath = imagePath,
            classifierScores = baselinePredictions
        )
        currentDimensionIndex = 0
        val currentDim = SinbadDimension.ORDERED_DIMENSIONS[0]
        state = TriageState.Questioning(currentDim, 0)

        val greeting = IndicTranslations.getGreeting(language)
        val firstQuestion = IndicTranslations.getQuestion(
            currentDim,
            language,
            baselinePredictions[currentDim]?.rationale
        )

        return listOf(
            ChatMessage(
                sender = MessageSender.ASSISTANT,
                text = greeting
            ),
            ChatMessage(
                sender = MessageSender.ASSISTANT,
                text = firstQuestion,
                dimension = currentDim
            )
        )
    }

    /**
     * Processes a user message in response to the current triage question.
     * Returns a list of generated reply messages and updates internal state.
     */
    fun processUserMessage(userText: String): List<ChatMessage> {
        val currentState = state
        if (currentState !is TriageState.Questioning) {
            return listOf(
                ChatMessage(
                    sender = MessageSender.ASSISTANT,
                    text = "Triage questioning has completed. Please review your Case Sheet below."
                )
            )
        }

        val dim = currentState.currentDimension
        val outgoingMessages = mutableListOf<ChatMessage>()

        // 1. Evaluate Medical Safety Guardrails
        when (val safetyResult = SafetyGuardrails.evaluate(userText, language)) {
            is SafetyCheckResult.Emergency -> {
                assessment = assessment.copy(
                    emergencyFlag = true,
                    emergencyReason = safetyResult.reason
                )
                outgoingMessages.add(
                    ChatMessage(
                        sender = MessageSender.EMERGENCY,
                        text = safetyResult.advisory
                    )
                )
            }
            is SafetyCheckResult.NonDiagnosticDeflection -> {
                outgoingMessages.add(
                    ChatMessage(
                        sender = MessageSender.ASSISTANT,
                        text = safetyResult.responseText
                    )
                )
                // Re-prompt for current dimension so we don't skip it
                outgoingMessages.add(
                    ChatMessage(
                        sender = MessageSender.ASSISTANT,
                        text = IndicTranslations.getQuestion(dim, language),
                        dimension = dim
                    )
                )
                return outgoingMessages
            }
            is SafetyCheckResult.Safe -> {
                // Continue with clinical triage parsing
            }
        }

        // 2. Parse patient confirmation for the current dimension
        val (score, clinicalSummary) = IndicTranslations.parsePatientResponse(dim, userText)
        val classifierPred = assessment.classifierScores[dim]
        val discrepancy = classifierPred != null && classifierPred.score != score

        val confirmation = PatientConfirmation(
            dimension = dim,
            score = score,
            patientRawQuote = userText.trim(),
            clinicalSummary = clinicalSummary,
            verified = true,
            discrepancyWithClassifier = discrepancy
        )

        val updatedPatientScores = assessment.patientScores.toMutableMap()
        updatedPatientScores[dim] = confirmation
        assessment = assessment.copy(patientScores = updatedPatientScores)

        // 3. Move to next dimension or complete
        currentDimensionIndex++
        if (currentDimensionIndex < SinbadDimension.ORDERED_DIMENSIONS.size) {
            val nextDim = SinbadDimension.ORDERED_DIMENSIONS[currentDimensionIndex]
            state = TriageState.Questioning(nextDim, currentDimensionIndex)

            val nextQuestion = IndicTranslations.getQuestion(
                nextDim,
                language,
                assessment.classifierScores[nextDim]?.rationale
            )

            val acknowledgment = when (language) {
                Language.HINDI -> "धन्यवाद, मैंने यह नोट कर लिया है।"
                Language.TAMIL -> "நன்றி, குறிப்பெடுத்துக் கொண்டேன்."
                Language.TELUGU -> "ధన్యవాదాలు, నేను దీనిని నమోదు చేసుకున్నాను."
                Language.KANNADA -> "ಧನ್ಯವಾದಗಳು, ಇದನ್ನು ದಾಖಲಿಸಲಾಗಿದೆ."
                Language.BENGALI -> "ধন্যবাদ, এটি লিপিবদ্ধ করেছি।"
                Language.MARATHI -> "धन्यवाद, मी हे नोंदवले आहे."
                else -> "Thank you, I have noted that."
            }

            outgoingMessages.add(
                ChatMessage(
                    sender = MessageSender.ASSISTANT,
                    text = "$acknowledgment\n\n$nextQuestion",
                    dimension = nextDim
                )
            )
        } else {
            // All dimensions complete
            state = TriageState.Completed(assessment)
            val completionMsg = when (language) {
                Language.HINDI ->
                    "सभी 6 प्रश्न पूरे हो गए हैं। आपकी केस शीट तैयार कर दी गई है। कृपया इसे अपने डॉक्टर को दिखाएं।"
                Language.TAMIL ->
                    "அனைத்து 6 கேள்விகளும் முடிவடைந்தன. உங்கள் கேஸ் ஷீட் தயாராக உள்ளது. இதை மருத்துவரிடம் காட்டவும்."
                Language.TELUGU ->
                    "అన్ని 6 ప్రశ్నలు పూర్తయ్యాయి. మీ కేస్ షీట్ సిద్ధంగా ఉంది. దయచేసి దీన్ని డాక్టర్‌కు చూపించండి."
                Language.KANNADA ->
                    "ಎಲ್ಲಾ 6 ಪ್ರಶ್ನೆಗಳು ಪೂರ್ಣಗೊಂಡಿವೆ. ನಿಮ್ಮ ಕೇಸ್ ಶೀಟ್ ಸಿದ್ಧವಾಗಿದೆ. ದಯವಿಟ್ಟು ಇದನ್ನು ನಿಮ್ಮ ವೈದ್ಯರಿಗೆ ತೋರಿಸಿ."
                Language.BENGALI ->
                    "সমস্ত ৬টি প্রশ্ন সম্পন্ন হয়েছে। আপনার কেস শীট তৈরি হয়েছে। দয়া করে এটি আপনার ডাক্তারকে দেখান।"
                Language.MARATHI ->
                    "सर्व 6 प्रश्न पूर्ण झाले आहेत. तुमची केस शीट तयार झाली आहे. कृपया ही तुमच्या डॉक्टरांना दाखवा."
                else ->
                    "All 6 triage questions have been answered. Your comprehensive Case Sheet is ready below for your doctor's review."
            }

            val referralNotice = SafetyGuardrails.getUniversalReferralNotice(language)

            outgoingMessages.add(
                ChatMessage(
                    sender = MessageSender.SYSTEM,
                    text = "$completionMsg\n\n$referralNotice"
                )
            )
        }

        return outgoingMessages
    }

    fun getCurrentAssessment(): SinbadAssessment = assessment
}
