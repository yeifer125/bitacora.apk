package com.tubitacora.plantas.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitGoogleClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    // 🔑 ACTUALIZADO: Usando la llave que funcionó en tu curl
    const val GEMINI_API_KEY = "#"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val service: GoogleGeminiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleGeminiService::class.java)
    }
}
