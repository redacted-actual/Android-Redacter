package com.redactedactual.redacter

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class RedactionEngine {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val emailRegex = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
    private val phoneRegex = Regex("(\\+\\d{1,3}[- ]?)?\\(?\\d{3}\\)?[- ]?\\d{3}[- ]?\\d{4}")
    private val ssnRegex = Regex("\\d{3}[- ]?\\d{2}[- ]?\\d{4}")

    suspend fun redact(
        bitmap: Bitmap,
        types: Set<RedactionType>
    ): Bitmap {
        if (types.isEmpty()) return bitmap

        val image = InputImage.fromBitmap(bitmap, 0)
        val result = recognizer.process(image).await()

        val output = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }

        for (block in result.textBlocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    val text = element.text
                    if (shouldRedact(text, types)) {
                        element.boundingBox?.let { box ->
                            canvas.drawRect(box, paint)
                        }
                    }
                }
            }
        }
        return output
    }

    private fun shouldRedact(text: String, types: Set<RedactionType>): Boolean {
        if (RedactionType.ALL in types) return true
        return (RedactionType.EMAIL in types && emailRegex.containsMatchIn(text)) ||
               (RedactionType.PHONE in types && phoneRegex.containsMatchIn(text)) ||
               (RedactionType.SSN in types && ssnRegex.containsMatchIn(text))
    }
}

enum class RedactionType { EMAIL, PHONE, SSN, ALL }
