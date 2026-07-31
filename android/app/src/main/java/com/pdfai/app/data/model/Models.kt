package com.pdfai.app.data.model

import com.google.gson.annotations.SerializedName

data class UploadResponse(
    val status: String,
    val message: String,
    val filename: String,
    @SerializedName("word_count") val wordCount: Int,
    @SerializedName("total_chunks") val totalChunks: Int
)

data class SummaryResponse(
    val filename: String?,
    val summary: String,
    @SerializedName("key_points") val keyPoints: List<String>,
    @SerializedName("reading_time_minutes") val readingTimeMinutes: Int,
    @SerializedName("word_count") val wordCount: Int,
    val topics: List<String>
)

data class QaRequest(
    val question: String,
    @SerializedName("top_k") val topK: Int = 3
)

data class QaResponse(
    val question: String,
    val answer: String,
    val sources: List<String>,
    @SerializedName("confidence_score") val confidenceScore: Float
)

data class Flashcard(
    val id: Int,
    val question: String,
    val answer: String,
    val category: String,
    val difficulty: String
)

data class FlashcardListResponse(
    val count: Int,
    val flashcards: List<Flashcard>
)

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    @SerializedName("correct_option_index") val correctOptionIndex: Int,
    val explanation: String
)

data class QuizListResponse(
    val count: Int,
    val quiz: List<QuizQuestion>
)

data class SimilarityRequest(
    val text1: String,
    val text2: String
)

data class SimilarityResponse(
    val text1: String,
    val text2: String,
    @SerializedName("tflite_cosine_similarity") val similarityScore: Float
)
