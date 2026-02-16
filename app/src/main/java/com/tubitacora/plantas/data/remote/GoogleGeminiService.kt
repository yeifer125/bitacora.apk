package com.tubitacora.plantas.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GoogleGeminiService {
    // ✅ CAMBIO: Usamos la API v1 (estable) con gemini-pro, en lugar de v1beta.
    // Esta es la URL más compatible y no debería dar 404.
    @POST("v1/models/gemini-pro:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

data class GeminiRequest(val contents: List<Content>)
data class Content(val parts: List<Part>)
data class Part(val text: String)

data class GeminiResponse(val candidates: List<Candidate>)
data class Candidate(val content: ContentResponse)
data class ContentResponse(val parts: List<Part>)
