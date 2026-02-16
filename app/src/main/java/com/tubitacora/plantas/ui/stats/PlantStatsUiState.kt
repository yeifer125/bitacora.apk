package com.tubitacora.plantas.ui.stats

import com.tubitacora.plantas.data.local.entity.PlantLogEntity

data class PlantStatsUiState(
    val plantName: String = "",
    val daysSincePlanting: Int = 0,

    // 📈 CRECIMIENTO
    val totalGrowthLogs: Int = 0,
    val currentHeightCm: Float = 0f,
    val previousHeightCm: Float? = null,
    val growthHistory: List<PlantLogEntity> = emptyList(), // ✅ AÑADIDO: Historial para el gráfico

    // 💧 RIEGO
    val totalWaterings: Int = 0,
    val lastWateredDaysAgo: Int = -1,
    val wateringFrequency: Int = 0,

    // 🌦️ CLIMA
    val rainDetected: Int = 0,
    val wateringAvoided: Int = 0,

    val healthStatus: String = "—"
)
