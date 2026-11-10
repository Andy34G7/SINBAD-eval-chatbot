package com.woundtriage.chat.data.classifier

import android.graphics.Bitmap
import com.woundtriage.chat.domain.model.ClassifierPrediction
import com.woundtriage.chat.domain.model.SinbadDimension

/**
 * Pluggable interface for on-device wound vision classification.
 */
interface WoundVisionClassifier {
    fun classify(bitmap: Bitmap?): Map<SinbadDimension, ClassifierPrediction>
}
