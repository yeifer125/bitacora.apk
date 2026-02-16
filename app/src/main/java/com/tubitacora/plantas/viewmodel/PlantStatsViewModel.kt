package com.tubitacora.plantas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tubitacora.plantas.data.local.AppDatabase
import com.tubitacora.plantas.data.local.entity.WeatherDecisionEntity
import com.tubitacora.plantas.ui.stats.PlantStatsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PlantStatsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val plantDao = db.plantDao()
    private val weatherDao = db.weatherDecisionDao()

    private val _uiState = MutableStateFlow(PlantStatsUiState())
    val uiState: StateFlow<PlantStatsUiState> = _uiState

    // ✅ NUEVO: Helper para calcular días de calendario
    private fun getDaysBetween(start: Long, end: Long): Int {
        val cal1 = Calendar.getInstance()
        cal1.timeInMillis = start
        cal1.set(Calendar.HOUR_OF_DAY, 0)
        cal1.set(Calendar.MINUTE, 0)
        cal1.set(Calendar.SECOND, 0)
        cal1.set(Calendar.MILLISECOND, 0)

        val cal2 = Calendar.getInstance()
        cal2.timeInMillis = end
        cal2.set(Calendar.HOUR_OF_DAY, 0)
        cal2.set(Calendar.MINUTE, 0)
        cal2.set(Calendar.SECOND, 0)
        cal2.set(Calendar.MILLISECOND, 0)

        val diff = cal2.timeInMillis - cal1.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }

    fun loadStats(plantId: Long) {
        viewModelScope.launch {

            val plant = plantDao.getPlantById(plantId) ?: return@launch

            // 📦 Logs
            val allLogs = plantDao.getLogsForPlant(plantId).first()
            val wateringLogs = allLogs.filter { it.watered }
            val growthLogs = allLogs.filter { it.heightCm > 0f }

            val now = System.currentTimeMillis()

            // 📅 Días desde siembra (LÓGICA CORREGIDA)
            val daysSincePlanting = getDaysBetween(plant.plantingDate, now)

            // 💧 Último riego
            val lastWateredTimestamp = when {
                plant.lastWatered > 0 -> plant.lastWatered
                wateringLogs.isNotEmpty() -> wateringLogs.maxOf { it.date }
                else -> 0L
            }

            val lastWateredDaysAgo =
                if (lastWateredTimestamp > 0)
                    getDaysBetween(lastWateredTimestamp, now) // LÓGICA CORREGIDA
                else
                    -1

            // 🌦️ Clima
            val weatherDecisions: List<WeatherDecisionEntity> =
                weatherDao.getAllForPlant(plantId).first()

            val rainDetected = weatherDecisions.count { !it.shouldWater }
            val wateringAvoided = weatherDecisions.count { !it.shouldWater }

            // 🩺 Estado de salud
            val healthStatus = when {
                lastWateredDaysAgo < 0 -> "Sin datos 🌱"
                lastWateredDaysAgo <= plant.wateringFrequencyDays -> "Saludable 🌱"
                lastWateredDaysAgo <= plant.wateringFrequencyDays + 2 -> "Atención ⚠️"
                else -> "Crítica 🚨"
            }

            // 📏 Altura actual y anterior
            val sortedGrowth = growthLogs.sortedBy { it.date }
            val previousHeight = sortedGrowth.getOrNull(sortedGrowth.size - 2)?.heightCm
            val currentHeight = sortedGrowth.lastOrNull()?.heightCm ?: 0f

            // ✅ Estado final
            _uiState.value = PlantStatsUiState(
                plantName = plant.name,
                daysSincePlanting = daysSincePlanting,

                // 📈 CRECIMIENTO
                totalGrowthLogs = growthLogs.size,
                currentHeightCm = currentHeight,
                previousHeightCm = previousHeight,
                growthHistory = sortedGrowth,

                // 💧 RIEGO
                totalWaterings = wateringLogs.size,
                lastWateredDaysAgo = lastWateredDaysAgo,
                wateringFrequency = plant.wateringFrequencyDays,

                // 🌦️ CLIMA
                rainDetected = rainDetected,
                wateringAvoided = wateringAvoided,

                healthStatus = healthStatus
            )
        }
    }
}
