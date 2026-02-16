package com.tubitacora.plantas.data.remote.dto

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)
