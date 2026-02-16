package com.tubitacora.plantas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_decisions")
data class WeatherDecisionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plantId: Long,
    val date: String,              // yyyy-MM-dd
    val shouldWater: Boolean,
    val conditionText: String
)
