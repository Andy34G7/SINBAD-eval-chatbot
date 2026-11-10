package com.woundtriage.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.woundtriage.chat.ui.screens.CaseSheetScreen
import com.woundtriage.chat.ui.screens.ChatScreen
import com.woundtriage.chat.ui.screens.WoundCaptureScreen
import com.woundtriage.chat.ui.theme.WoundTriageTheme
import com.woundtriage.chat.ui.viewmodel.CurrentScreen
import com.woundtriage.chat.ui.viewmodel.TriageViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TriageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WoundTriageTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val uiState by viewModel.uiState.collectAsState()

                    BackHandler(enabled = uiState.currentScreen != CurrentScreen.CAPTURE) {
                        when (uiState.currentScreen) {
                            CurrentScreen.CASE_SHEET -> viewModel.navigateTo(CurrentScreen.CHAT)
                            CurrentScreen.CHAT -> viewModel.navigateTo(CurrentScreen.CAPTURE)
                            CurrentScreen.CAPTURE -> Unit
                        }
                    }

                    when (uiState.currentScreen) {
                        CurrentScreen.CAPTURE -> {
                            WoundCaptureScreen(
                                uiState = uiState,
                                onLanguageSelected = { viewModel.setLanguage(it) },
                                onImageCaptured = { viewModel.onImageSelected(it) }
                            )
                        }
                        CurrentScreen.CHAT -> {
                            ChatScreen(
                                uiState = uiState,
                                onSendMessage = { viewModel.sendMessage(it) },
                                onNavigateToCaseSheet = { viewModel.navigateTo(CurrentScreen.CASE_SHEET) },
                                onBack = { viewModel.navigateTo(CurrentScreen.CAPTURE) }
                            )
                        }
                        CurrentScreen.CASE_SHEET -> {
                            CaseSheetScreen(
                                uiState = uiState,
                                onGeneratePdf = { viewModel.generateCaseSheetPdf() },
                                onReset = { viewModel.resetTriage() },
                                onBack = { viewModel.navigateTo(CurrentScreen.CHAT) }
                            )
                        }
                    }
                }
            }
        }
    }
}
