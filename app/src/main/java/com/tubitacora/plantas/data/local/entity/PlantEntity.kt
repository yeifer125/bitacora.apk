package com.tubitacora.plantas.data.local.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "plants")
data class PlantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val plantingDate: Long,
    val wateringFrequencyDays: Int,
    val lastWatered: Long= 0L,
    val notes: String?,
    val reminderActive: Boolean = false,
    val customDelayMillis: Long? = null
) : Parcelable
