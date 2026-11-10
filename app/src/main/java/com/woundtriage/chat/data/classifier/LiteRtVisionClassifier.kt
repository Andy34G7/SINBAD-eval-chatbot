package com.woundtriage.chat.data.classifier

import android.content.Context
import android.graphics.Bitmap
import com.woundtriage.chat.domain.model.ClassifierPrediction
import com.woundtriage.chat.domain.model.SinbadDimension
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * LiteRT (TensorFlow Lite) on-device vision classifier for SINBAD dimensions.
 * Gracefully falls back to HeuristicVisionClassifier when model asset is missing.
 */
class LiteRtVisionClassifier(
    private val context: Context,
    private val modelAssetPath: String = "models/sinbad_vision.tflite"
) : WoundVisionClassifier {

    private val fallback = HeuristicVisionClassifier()
    private var interpreter: Interpreter? = null

    init {
        initInterpreter()
    }

    private fun initInterpreter() {
        try {
            val assetFileDescriptor = context.assets.openFd(modelAssetPath)
            val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

            val options = Interpreter.Options().apply {
                setNumThreads(2)
            }
            interpreter = Interpreter(modelBuffer, options)
        } catch (_: Exception) {
            // Model file not present in assets or failed to load; using heuristic classifier
            interpreter = null
        }
    }

    override fun classify(bitmap: Bitmap?): Map<SinbadDimension, ClassifierPrediction> {
        val activeInterpreter = interpreter
        if (activeInterpreter == null || bitmap == null) {
            return fallback.classify(bitmap)
        }

        return try {
            // Prepare 224x224 normalized RGB input buffer
            val inputSize = 224
            val scaled = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
            val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
            byteBuffer.order(ByteOrder.nativeOrder())

            val intValues = IntArray(inputSize * inputSize)
            scaled.getPixels(intValues, 0, inputSize, 0, 0, inputSize, inputSize)

            for (pixel in intValues) {
                val r = (pixel shr 16 and 0xFF) / 255.0f
                val g = (pixel shr 8 and 0xFF) / 255.0f
                val b = (pixel and 0xFF) / 255.0f
                byteBuffer.putFloat(r)
                byteBuffer.putFloat(g)
                byteBuffer.putFloat(b)
            }

            // Output array: 6 dimension probabilities
            val output = Array(1) { FloatArray(6) }
            activeInterpreter.run(byteBuffer, output)

            val probs = output[0]
            SinbadDimension.ORDERED_DIMENSIONS.mapIndexed { idx, dim ->
                val prob = probs.getOrElse(idx) { 0.5f }
                val score = if (prob >= 0.5f) 1 else 0
                dim to ClassifierPrediction(
                    dimension = dim,
                    score = score,
                    confidence = prob.coerceIn(0.5f, 0.99f),
                    rationale = "LiteRT vision model evaluated probability: ${(prob * 100).toInt()}%"
                )
            }.toMap()
        } catch (_: Exception) {
            fallback.classify(bitmap)
        }
    }
}
