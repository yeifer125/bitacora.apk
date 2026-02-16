package com.tubitacora.plantas.data.remote.dto

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("role")
    val role: String,

    @SerializedName("content")
    val content: String
)
