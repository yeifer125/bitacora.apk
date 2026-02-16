package com.tubitacora.plantas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plant_logs")
data class PlantLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plantId: Long,
    val date: Long,
    val heightCm: Float = 0f,
    val watered: Boolean = false,
    val note: String? = null,
    val status: String,
    val fertilizerType: String? = null,   // NUEVO
    val fertilizerDose: String? = null

)
