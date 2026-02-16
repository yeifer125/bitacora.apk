package com.tubitacora.plantas.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.tubitacora.plantas.data.local.AppDatabase
import com.tubitacora.plantas.data.local.entity.PlantEntity
import com.tubitacora.plantas.data.local.entity.PlantLogEntity
import com.tubitacora.plantas.workers.WaterReminderWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class PlantViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val plantDao = db.plantDao()
    private val workManager = WorkManager.getInstance(application)

    val plants = plantDao.getAllPlants()

    fun insertPlant(plant: PlantEntity) {
        viewModelScope.launch {
            plantDao.insert(plant)
        }
    }

    fun updatePlant(plant: PlantEntity) {
        viewModelScope.launch {
            plantDao.update(plant)
        }
    }

    fun deletePlant(plant: PlantEntity) {
        viewModelScope.launch {
            cancelWateringReminder(plant.id)
            plantDao.delete(plant)
        }
    }

    // ---------- LOGS ----------
    fun addWateringLog(plantId: Long) {
        viewModelScope.launch {
            val plant = plantDao.getPlantById(plantId) ?: return@launch

            // 1. Crear el registro en el historial
            plantDao.insertLog(
                PlantLogEntity(
                    plantId = plantId,
                    date = System.currentTimeMillis(),
                    watered = true,
                    status = "Riego"
                )
            )
            
            // 2. ✅ Actualizar la fecha del último riego en la planta principal
            val updatedPlant = plant.copy(lastWatered = System.currentTimeMillis())
            plantDao.update(updatedPlant)

            // 3. Reprogramar recordatorio si está activo
            if (updatedPlant.reminderActive) {
                scheduleSmartWatering(
                    context = getApplication(),
                    plantId = updatedPlant.id,
                    plantName = updatedPlant.name,
                    frequencyDays = updatedPlant.wateringFrequencyDays,
                    lastWatered = updatedPlant.lastWatered,
                    customDelayMillis = updatedPlant.customDelayMillis
                )
            }
        }
    }

    // ---------- ⏰ RIEGO INTELIGENTE (WorkManager) ----------
    fun scheduleSmartWatering(
        context: Context,
        plantId: Long,
        plantName: String,
        frequencyDays: Int,
        lastWatered: Long,
        customDelayMillis: Long? = null
    ) {
        val delayMillis = customDelayMillis
            ?: (TimeUnit.DAYS.toMillis(frequencyDays.toLong()))

        val inputData = workDataOf(
            "plantId" to plantId,
            "plantName" to plantName
        )

        val wateringWorkRequest = OneTimeWorkRequestBuilder<WaterReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("watering_$plantId")
            .build()

        workManager.enqueueUniqueWork(
            "watering_work_$plantId",
            ExistingWorkPolicy.REPLACE,
            wateringWorkRequest
        )

        Log.d("Riego", "Recordatorio programado para $plantName en ${delayMillis/1000/60} minutos")
    }

    fun cancelWateringReminder(plantId: Long) {
        workManager.cancelUniqueWork("watering_work_$plantId")
        Log.d("Riego", "Recordatorio cancelado para planta ID: $plantId")
    }

    fun updateReminderState(
        plantId: Long,
        active: Boolean,
        customDelayMillis: Long? = null
    ) {
        viewModelScope.launch {
            val plant = plantDao.getPlantById(plantId) ?: return@launch
            val updatedPlant = plant.copy(
                reminderActive = active,
                customDelayMillis = customDelayMillis
            )
            plantDao.update(updatedPlant)

            if (active) {
                scheduleSmartWatering(
                    context = getApplication(),
                    plantId = updatedPlant.id,
                    plantName = updatedPlant.name,
                    frequencyDays = updatedPlant.wateringFrequencyDays,
                    lastWatered = updatedPlant.lastWatered,
                    customDelayMillis = updatedPlant.customDelayMillis
                )
            } else {
                cancelWateringReminder(plantId)
            }
        }
    }
}
