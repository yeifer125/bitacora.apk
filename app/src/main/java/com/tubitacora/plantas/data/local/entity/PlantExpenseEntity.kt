package com.tubitacora.plantas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PlantExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val date: Long = System.currentTimeMillis(),
    val amount: Float,
    val note: String? = null
)
