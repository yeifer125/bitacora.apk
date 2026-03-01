package com.tubitacora.plantas.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GoogleGeminiService {
    // ✅ Probamos con la ruta v1beta y el modelo gemini-1.5-flash.
    // ✅ Esta es la combinación exacta que usa Google AI Studio actualmente.
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

data class GeminiRequest(val contents: List<Content>)
data class Content(val parts: List<Part>)
data class Part(val text: String)

data class GeminiResponse(
    val candidates: List<Candidate>?,
    val error: GeminiError?
)

data class Candidate(val content: ContentResponse)
data class ContentResponse(val parts: List<Part>)
data class GeminiError(val message: String?, val code: Int?)
