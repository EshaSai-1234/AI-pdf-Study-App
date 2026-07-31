package com.pdfai.app.data.api

import com.pdfai.app.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface PdfAiApiService {

    @Multipart
    @POST("api/upload")
    suspend fun uploadPdf(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @GET("api/summary")
    suspend fun getSummary(): Response<SummaryResponse>

    @POST("api/qa")
    suspend fun askQuestion(
        @Body request: QaRequest
    ): Response<QaResponse>

    @GET("api/flashcards")
    suspend fun getFlashcards(
        @Query("count") count: Int = 5
    ): Response<FlashcardListResponse>

    @GET("api/quiz")
    suspend fun getQuiz(
        @Query("count") count: Int = 5
    ): Response<QuizListResponse>

    @POST("api/tflite/similarity")
    suspend fun calculateTFLiteSimilarity(
        @Body request: SimilarityRequest
    ): Response<SimilarityResponse>
}
