package com.tubitacora.plantas.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitDeepSeekClient {
    private const val BASE_URL = "https://api.deepseek.com/"
    
    // 🔑 REEMPLAZA ESTO CON TU API KEY REAL DE DEEPSEEK
    private const val API_KEY = "AIzaSyBR2vYNlaAENUZVvyyva9kmgmmkQT5PZzI"

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $API_KEY")
            .build()
        chain.proceed(request)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(authInterceptor) // ✅ Agregamos la llave automáticamente
        .build()

    val service: DeepSeekService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeepSeekService::class.java)
    }
}
