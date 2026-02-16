package com.tubitacora.plantas.data.remote.dto

data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val max_tokens: Int,
    val temperature: Double
)
