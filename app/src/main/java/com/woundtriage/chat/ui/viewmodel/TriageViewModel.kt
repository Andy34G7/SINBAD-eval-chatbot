package com.woundtriage.chat.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.woundtriage.chat.data.classifier.LiteRtVisionClassifier
import com.woundtriage.chat.data.classifier.WoundVisionClassifier
import com.woundtriage.chat.data.llm.MediaPipeLlmEngine
import com.woundtriage.chat.data.llm.TriageLlmEngine
import com.woundtriage.chat.data.pdf.CaseSheetPdfGenerator
import com.woundtriage.chat.domain.model.ChatMessage
import com.woundtriage.chat.domain.model.Language
import com.woundtriage.chat.domain.model.MessageSender
import com.woundtriage.chat.domain.model.SinbadAssessment
import com.woundtriage.chat.domain.statemachine.TriageState
import com.woundtriage.chat.domain.statemachine.TriageStateMachine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class CurrentScreen {
    CAPTURE,
    CHAT,
    CASE_SHEET
}

data class TriageUiState(
    val currentScreen: CurrentScreen = CurrentScreen.CAPTURE,
    val selectedLanguage: Language = Language.ENGLISH,
    val capturedImageBitmap: Bitmap? = null,
    val capturedImagePath: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val assessment: SinbadAssessment = SinbadAssessment(),
    val triageState: TriageState = TriageState.AwaitingImage,
    val isStreaming: Boolean = false,
    val streamingText: String = "",
    val isProcessing: Boolean = false,
    val generatedPdfFile: File? = null,
    val errorMessage: String? = null
)

class TriageViewModel(
    application: Application,
    private val visionClassifier: WoundVisionClassifier = LiteRtVisionClassifier(application),
    private val llmEngine: TriageLlmEngine = MediaPipeLlmEngine(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TriageUiState())
    val uiState: StateFlow<TriageUiState> = _uiState.asStateFlow()

    private var stateMachine = TriageStateMachine(language = Language.ENGLISH)
    private val pdfGenerator = CaseSheetPdfGenerator(application)

    init {
        // Initialize MediaPipe LLM in background if applicable
        if (llmEngine is MediaPipeLlmEngine) {
            viewModelScope.launch(Dispatchers.IO) {
                llmEngine.initialize()
            }
        }
    }

    fun setLanguage(language: Language) {
        val currentAssessment = _uiState.value.assessment.copy(patientLanguage = language)
        _uiState.update { it.copy(selectedLanguage = language, assessment = currentAssessment) }
        stateMachine = TriageStateMachine(
            language = language,
            initialAssessment = currentAssessment
        )
    }

    fun onImageSelected(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

            // Save bitmap to app cache
            val cacheFile = File(getApplication<Application>().cacheDir, "captured_wound_${System.currentTimeMillis()}.jpg")
            withContext(Dispatchers.IO) {
                FileOutputStream(cacheFile).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                }
            }

            // Run vision classifier
            val predictions = visionClassifier.classify(bitmap)

            // Start triage state machine
            val initialMsgs = stateMachine.startWithClassifierBaseline(
                imagePath = cacheFile.absolutePath,
                baselinePredictions = predictions
            )

            _uiState.update {
                it.copy(
                    currentScreen = CurrentScreen.CHAT,
                    capturedImageBitmap = bitmap,
                    capturedImagePath = cacheFile.absolutePath,
                    messages = initialMsgs,
                    assessment = stateMachine.getCurrentAssessment(),
                    triageState = stateMachine.state,
                    isProcessing = false
                )
            }
        }
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return

        val userMessage = ChatMessage(
            sender = MessageSender.USER,
            text = trimmed
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                isStreaming = true,
                streamingText = ""
            )
        }

        viewModelScope.launch(Dispatchers.Default) {
            val responseMessages = stateMachine.processUserMessage(trimmed)
            val updatedAssessment = stateMachine.getCurrentAssessment()

            val finalMessages = mutableListOf<ChatMessage>()
            for (msg in responseMessages) {
                if (msg.sender == MessageSender.ASSISTANT) {
                    val streamFlow = llmEngine.generateStream(msg.text)
                    var accumulated = ""
                    streamFlow.collect { token ->
                        accumulated += token
                        _uiState.update { it.copy(streamingText = accumulated) }
                    }
                    finalMessages.add(msg.copy(text = accumulated.ifEmpty { msg.text }))
                } else {
                    finalMessages.add(msg)
                }
            }

            _uiState.update {
                it.copy(
                    messages = it.messages + finalMessages,
                    assessment = updatedAssessment,
                    triageState = stateMachine.state,
                    isStreaming = false,
                    streamingText = ""
                )
            }
        }
    }

    fun navigateTo(screen: CurrentScreen) {
        if (screen == CurrentScreen.CASE_SHEET) {
            generateCaseSheetPdf()
        }
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun generateCaseSheetPdf(): File? {
        val assessment = _uiState.value.assessment
        return try {
            val pdfFile = File(
                getApplication<Application>().cacheDir,
                "CaseSheet_${assessment.id}.pdf"
            )
            pdfGenerator.generatePdfFile(assessment, pdfFile)
            _uiState.update { it.copy(generatedPdfFile = pdfFile) }
            pdfFile
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "PDF generation failed: ${e.localizedMessage}") }
            null
        }
    }

    fun resetTriage() {
        val currentLang = _uiState.value.selectedLanguage
        stateMachine = TriageStateMachine(language = currentLang)
        _uiState.update {
            TriageUiState(
                currentScreen = CurrentScreen.CAPTURE,
                selectedLanguage = currentLang
            )
        }
    }
}
