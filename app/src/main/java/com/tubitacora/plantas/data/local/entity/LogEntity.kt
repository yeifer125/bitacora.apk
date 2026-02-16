package com.tubitacora.plantas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plantId: Long,
    val date: Long,
    val watered: Boolean,
    val fertilizer: String? = null,
    val status: String, // Ej: "Regada", "Abonada", "Hoja marchita"
    val observations: String? = null, // Nota extra
    val photoUri: String? = null // <-- nueva para foto opcional
)

