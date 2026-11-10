package com.woundtriage.chat.data.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.woundtriage.chat.domain.model.SinbadAssessment
import com.woundtriage.chat.domain.model.SinbadDimension
import java.io.File
import java.io.FileOutputStream

/**
 * High-resolution native Android PdfDocument generator for the SINBAD Case Sheet.
 * Works completely offline with zero external PDF dependencies.
 */
class CaseSheetPdfGenerator(private val context: Context) {

    companion object {
        private const val PAGE_WIDTH = 595 // Standard A4 width in PostScript points
        private const val PAGE_HEIGHT = 842 // Standard A4 height in PostScript points
    }

    fun generatePdfFile(assessment: SinbadAssessment, outputFile: File): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawCaseSheet(canvas, assessment)

        document.finishPage(page)

        FileOutputStream(outputFile).use { fos ->
            document.writeTo(fos)
        }
        document.close()

        return outputFile
    }

    private fun drawCaseSheet(canvas: Canvas, assessment: SinbadAssessment) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        canvas.drawColor(Color.WHITE)

        var y = 32f

        // 1. Header Banner
        paint.color = Color.rgb(15, 118, 110) // Teal 700
        canvas.drawRect(28f, y, PAGE_WIDTH - 28f, y + 42f, paint)

        paint.color = Color.WHITE
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("WOUND TRIAGE CASE SHEET (SINBAD VERIFICATION)", 38f, y + 26f, paint)

        y += 52f

        // 2. Metadata details
        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 9f
        paint.isFakeBoldText = false

        canvas.drawText("Encounter ID: #${assessment.id}", 32f, y, paint)
        canvas.drawText("Date & Time: ${assessment.formattedDate}", 180f, y, paint)
        canvas.drawText("Language: ${assessment.patientLanguage.englishName} (${assessment.patientLanguage.nativeName})", 380f, y, paint)

        y += 16f

        // Divider
        paint.color = Color.rgb(226, 232, 240)
        paint.strokeWidth = 1f
        canvas.drawLine(28f, y, PAGE_WIDTH - 28f, y, paint)

        y += 14f

        // 3. Wound Image & Score Summary Section
        val woundBitmap = loadWoundBitmap(assessment.imagePath)
        val imageSize = 100f
        if (woundBitmap != null) {
            val destRect = RectF(32f, y, 32f + imageSize, y + imageSize)
            canvas.drawBitmap(woundBitmap, null, destRect, paint)

            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(203, 213, 225)
            canvas.drawRect(destRect, paint)
            paint.style = Paint.Style.FILL
        } else {
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRect(32f, y, 32f + imageSize, y + imageSize, paint)
            paint.color = Color.rgb(100, 116, 139)
            paint.textSize = 9f
            canvas.drawText("No Photo", 60f, y + 55f, paint)
        }

        // Summary Card on right of image
        val summaryLeft = 148f
        val summaryTop = y
        val summaryWidth = PAGE_WIDTH - 28f - summaryLeft
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(summaryLeft, summaryTop, PAGE_WIDTH - 28f, summaryTop + imageSize, 8f, 8f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 11.5f
        paint.isFakeBoldText = true
        canvas.drawText("Overall SINBAD Score: ${assessment.totalScore} / 6", summaryLeft + 12f, summaryTop + 20f, paint)

        paint.textSize = 9.5f
        val severityColor = when (assessment.totalScore) {
            in 0..1 -> Color.rgb(22, 163, 74) // Green
            in 2..3 -> Color.rgb(217, 119, 6) // Amber
            else -> Color.rgb(220, 38, 38) // Red
        }
        paint.color = severityColor
        canvas.drawText("Clinical Risk Band: ${assessment.severity.title}", summaryLeft + 12f, summaryTop + 36f, paint)

        paint.color = Color.rgb(71, 85, 105)
        paint.isFakeBoldText = false
        paint.textSize = 8f
        canvas.drawText("• S - Site: ${assessment.patientScores[SinbadDimension.SITE]?.score ?: 0} pt", summaryLeft + 12f, summaryTop + 52f, paint)
        canvas.drawText("• I - Ischemia: ${assessment.patientScores[SinbadDimension.ISCHEMIA]?.score ?: 0} pt", summaryLeft + 110f, summaryTop + 52f, paint)
        canvas.drawText("• N - Neuropathy: ${assessment.patientScores[SinbadDimension.NEUROPATHY]?.score ?: 0} pt", summaryLeft + 220f, summaryTop + 52f, paint)

        canvas.drawText("• B - Infection: ${assessment.patientScores[SinbadDimension.BACTERIAL_INFECTION]?.score ?: 0} pt", summaryLeft + 12f, summaryTop + 68f, paint)
        canvas.drawText("• A - Area: ${assessment.patientScores[SinbadDimension.AREA]?.score ?: 0} pt", summaryLeft + 110f, summaryTop + 68f, paint)
        canvas.drawText("• D - Depth: ${assessment.patientScores[SinbadDimension.DEPTH]?.score ?: 0} pt", summaryLeft + 220f, summaryTop + 68f, paint)

        if (assessment.emergencyFlag) {
            paint.color = Color.rgb(220, 38, 38)
            paint.isFakeBoldText = true
            val alert = "⚠ EMERGENCY DETECTED: ${assessment.emergencyReason ?: "High risk infection"}"
            val clippedAlert = ellipsize(alert, paint, summaryWidth - 24f)
            canvas.drawText(clippedAlert, summaryLeft + 12f, summaryTop + 86f, paint)
        }

        y += imageSize + 16f

        // 4. Comparative Verification Table Header
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRect(28f, y, PAGE_WIDTH - 28f, y + 20f, paint)

        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 8f
        paint.isFakeBoldText = true

        canvas.drawText("DIMENSION", 34f, y + 14f, paint)
        canvas.drawText("CLASSIFIER BASELINE", 112f, y + 14f, paint)
        canvas.drawText("PATIENT CONFIRMATION & QUOTE", 236f, y + 14f, paint)
        canvas.drawText("SCORE", PAGE_WIDTH - 65f, y + 14f, paint)

        y += 24f

        // Table Rows
        paint.isFakeBoldText = false
        paint.textSize = 7.5f

        for (dim in SinbadDimension.ORDERED_DIMENSIONS) {
            val clf = assessment.classifierScores[dim]
            val pt = assessment.patientScores[dim]
            val confirmedScore = pt?.score ?: clf?.score ?: 0
            val isDiscrepancy = pt != null && clf != null && pt.score != clf.score

            // Alternating row background for discrepancies
            if (isDiscrepancy) {
                paint.color = Color.rgb(254, 242, 242) // subtle reddish highlight
                canvas.drawRect(28f, y - 4f, PAGE_WIDTH - 28f, y + 36f, paint)
            }

            paint.color = Color.rgb(15, 23, 42)
            paint.isFakeBoldText = true
            canvas.drawText("${dim.key} - ${dim.title}", 34f, y + 10f, paint)

            paint.isFakeBoldText = false
            paint.color = Color.rgb(71, 85, 105)
            val clfDesc = if (clf?.score == 1) dim.oneDesc else dim.zeroDesc
            val ellipClf = ellipsize(clfDesc, paint, 118f)
            canvas.drawText(ellipClf, 112f, y + 10f, paint)

            val quote = pt?.patientRawQuote?.let { "\"$it\"" } ?: "Pending"
            val ellipQuote = ellipsize(quote, paint, 260f)
            canvas.drawText(ellipQuote, 236f, y + 10f, paint)

            val summary = pt?.clinicalSummary ?: "Not answered"
            val ellipSummary = ellipsize(summary, paint, 260f)
            canvas.drawText(ellipSummary, 236f, y + 22f, paint)

            paint.isFakeBoldText = true
            paint.color = if (confirmedScore == 1) Color.rgb(185, 28, 28) else Color.rgb(21, 128, 61)
            canvas.drawText("$confirmedScore pt", PAGE_WIDTH - 60f, y + 14f, paint)

            y += 40f
        }

        y += 8f

        // Reported Symptoms Ledger Box (if present)
        if (assessment.reportedSymptoms.isNotEmpty()) {
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRoundRect(28f, y, PAGE_WIDTH - 28f, y + 32f, 4f, 4f, paint)
            paint.color = Color.rgb(30, 41, 59)
            paint.textSize = 8f
            paint.isFakeBoldText = true
            canvas.drawText("REPORTED SYMPTOMS:", 36f, y + 12f, paint)
            paint.isFakeBoldText = false
            paint.color = Color.rgb(71, 85, 105)
            val symptomsText = ellipsize(assessment.reportedSymptoms.joinToString(" • "), paint, PAGE_WIDTH - 80f)
            canvas.drawText(symptomsText, 36f, y + 24f, paint)
            y += 40f
        }

        // 5. Universal Physician Referral Disclaimer Box
        val noticeBoxTop = y
        val noticeBoxHeight = 84f
        paint.color = Color.rgb(254, 243, 199) // Amber 100
        canvas.drawRoundRect(28f, noticeBoxTop, PAGE_WIDTH - 28f, noticeBoxTop + noticeBoxHeight, 6f, 6f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(217, 119, 6)
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(28f, noticeBoxTop, PAGE_WIDTH - 28f, noticeBoxTop + noticeBoxHeight, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(146, 64, 14)
        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("MANDATORY CLINICAL REFERRAL & NON-DIAGNOSTIC NOTICE", 38f, noticeBoxTop + 16f, paint)

        paint.textSize = 7.5f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(120, 53, 15)
        canvas.drawText("• This report is an AI-assisted triage document generated directly on-device to structure wound evaluation.", 38f, noticeBoxTop + 30f, paint)
        canvas.drawText("• It DOES NOT provide a definitive diagnosis, treatment plan, or medication prescription.", 38f, noticeBoxTop + 44f, paint)
        canvas.drawText("• Universal Recommendation: All patients must undergo an in-person clinical examination by a physician or specialist.", 38f, noticeBoxTop + 58f, paint)
        canvas.drawText("• If signs of systemic toxicity, expanding cellulitis, or gangrene are present, seek immediate emergency medical care.", 38f, noticeBoxTop + 72f, paint)

        // Footer
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 7.5f
        canvas.drawText("Generated locally on device via LiteRT & MediaPipe GenAI • Confidential Medical Triage Record", 110f, PAGE_HEIGHT - 20f, paint)
    }

    private fun ellipsize(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var truncated = text
        while (truncated.isNotEmpty() && paint.measureText("$truncated…") > maxWidth) {
            truncated = truncated.dropLast(1)
        }
        return "$truncated…"
    }

    private fun loadWoundBitmap(path: String?): Bitmap? {
        if (path == null) return null
        return try {
            val file = File(path)
            if (!file.exists()) return null
            val options = BitmapFactory.Options().apply {
                inSampleSize = 2
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (_: Exception) {
            null
        }
    }
}
