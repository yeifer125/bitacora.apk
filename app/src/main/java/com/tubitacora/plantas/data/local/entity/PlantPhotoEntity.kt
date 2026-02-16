package com.tubitacora.plantas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plant_photos")
data class PlantPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val uri: String,
    val timestamp: Long
)
