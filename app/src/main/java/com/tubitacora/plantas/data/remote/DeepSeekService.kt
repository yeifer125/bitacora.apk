package com.tubitacora.plantas.data.remote

import com.tubitacora.plantas.data.remote.dto.ChatRequest
import com.tubitacora.plantas.data.remote.dto.ChatResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DeepSeekService {
    @POST("v1/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): ChatResponse
}
