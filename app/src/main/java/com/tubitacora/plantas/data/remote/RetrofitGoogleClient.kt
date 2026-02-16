package com.tubitacora.plantas.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitGoogleClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    val service: GoogleGeminiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleGeminiService::class.java)
    }
}
