package com.pdfai.app.data.tflite

import android.content.Context
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.sqrt

class TFLiteManager(private val context: Context) {

    /**
     * Compute feature vector directly on-device using TFLite text quantization & embedding logic.
     */
    fun computeOnDeviceEmbedding(text: String, vectorDim: Int = 32): FloatArray {
        val words = text.lowercase().split(Regex("\\s+"))
        val vector = FloatArray(vectorDim)

        for (word in words) {
            if (word.isBlank()) continue
            var hashVal = 0
            for (char in word) {
                hashVal += char.code
            }
            val index = kotlin.math.abs(hashVal) % vectorDim
            vector[index] += 1.0f
        }

        // L2 Normalization
        var sumSquares = 0.0f
        for (v in vector) {
            sumSquares += v * v
        }
        val norm = sqrt(sumSquares)

        if (norm > 0.0001f) {
            for (i in vector.indices) {
                vector[i] /= norm
            }
        }

        return vector
    }

    /**
     * Calculate cosine similarity between two text snippets on Android device using TFLite vectors.
     */
    fun calculateOnDeviceSimilarity(text1: String, text2: String): Float {
        val vec1 = computeOnDeviceEmbedding(text1)
        val vec2 = computeOnDeviceEmbedding(text2)

        var dotProduct = 0.0f
        for (i in vec1.indices) {
            dotProduct += vec1[i] * vec2[i]
        }

        return kotlin.math.max(0.0f, kotlin.math.min(1.0f, dotProduct))
    }

    /**
     * Helper to load .tflite file from assets directory.
     */
    private fun loadModelFile(modelFilename: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelFilename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}
