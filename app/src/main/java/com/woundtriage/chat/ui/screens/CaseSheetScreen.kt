package com.woundtriage.chat.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.woundtriage.chat.domain.model.SinbadDimension
import com.woundtriage.chat.ui.theme.EmergencyRed
import com.woundtriage.chat.ui.theme.EmergencyRedBg
import com.woundtriage.chat.ui.theme.TealPrimary
import com.woundtriage.chat.ui.theme.WarningAmberBg
import com.woundtriage.chat.ui.viewmodel.TriageUiState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseSheetScreen(
    uiState: TriageUiState,
    onGeneratePdf: () -> File?,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val assessment = uiState.assessment

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Clinical Case Sheet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val pdf = onGeneratePdf()
                        if (pdf != null) {
                            sharePdf(context, pdf)
                        } else {
                            Toast.makeText(context, "Generating PDF...", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share PDF", tint = TealPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Metadata Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Encounter: #${assessment.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Date: ${assessment.formattedDate}", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Language", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(
                            text = "${assessment.patientLanguage.nativeName} (${assessment.patientLanguage.englishName})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wound Photo & Score Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo Thumbnail
                if (uiState.capturedImageBitmap != null) {
                    Image(
                        bitmap = uiState.capturedImageBitmap.asImageBitmap(),
                        contentDescription = "Wound Photo",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Photo", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Total SINBAD Score",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "${assessment.totalScore} / 6",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = assessment.severity.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (assessment.totalScore) {
                            in 0..1 -> Color(0xFF16A34A)
                            in 2..3 -> Color(0xFFD97706)
                            else -> Color(0xFFDC2626)
                        }
                    )
                }
            }

            // Emergency Notice if applicable
            if (assessment.emergencyFlag) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmergencyRedBg)
                        .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Urgent Flag Triggered", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmergencyRed)
                            Text(assessment.emergencyReason ?: "Systemic infection signs reported.", fontSize = 11.sp, color = EmergencyRed)
                        }
                    }
                }
            }

            // Reported Symptoms Section if available
            if (assessment.reportedSymptoms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("Patient-Reported Symptoms", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = assessment.reportedSymptoms.joinToString(" • "),
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SINBAD Comparative Verification Checklist
            Text(
                text = "SINBAD Verification Checklist",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                    .background(Color.White)
            ) {
                SinbadDimension.ORDERED_DIMENSIONS.forEachIndexed { index, dim ->
                    val clf = assessment.classifierScores[dim]
                    val pt = assessment.patientScores[dim]
                    val confirmedScore = pt?.score ?: clf?.score ?: 0
                    val isDiscrepancy = pt != null && clf != null && pt.score != clf.score

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDiscrepancy) Color(0xFFFFF5F5) else Color.White)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${dim.key} - ${dim.title}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "$confirmedScore pt",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (confirmedScore == 1) Color(0xFFDC2626) else Color(0xFF16A34A)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Model Baseline: ${if (clf?.score == 1) dim.oneDesc else dim.zeroDesc}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )

                        if (pt != null) {
                            Text(
                                text = "Patient Quote: \"${pt.patientRawQuote}\"",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "Verified: ${pt.clinicalSummary}",
                                fontSize = 11.sp,
                                color = Color(0xFF0F766E)
                            )
                        }

                        if (isDiscrepancy) {
                            Text(
                                text = "⚠ Discrepancy noted between vision baseline and patient verification",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFB91C1C)
                            )
                        }
                    }

                    if (index < SinbadDimension.ORDERED_DIMENSIONS.size - 1) {
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Universal Referral Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(WarningAmberBg)
                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Universal Doctor Referral & Policy",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF92400E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This triage application is strictly non-diagnostic. We recommend presenting this Case Sheet directly to a doctor or foot care specialist for an in-person clinical examination.",
                        fontSize = 11.sp,
                        color = Color(0xFFB45309),
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Button(
                onClick = {
                    val file = onGeneratePdf()
                    if (file != null) {
                        sharePdf(context, file)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Print / Save PDF Case Sheet")
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onReset,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start New Triage Session")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun sharePdf(context: Context, pdfFile: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Case Sheet PDF"))
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open share dialog", Toast.LENGTH_SHORT).show()
    }
}
