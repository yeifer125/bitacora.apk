package com.tubitacora.plantas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tubitacora.plantas.data.local.AppDatabase
import com.tubitacora.plantas.data.local.dao.PlantExpenseDao
import com.tubitacora.plantas.data.local.entity.PlantLogEntity
import com.tubitacora.plantas.data.local.entity.PlantExpenseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LogViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val plantDao = db.plantDao()
    private val expenseDao: PlantExpenseDao = db.plantExpenseDao()

    fun getLogsForPlant(plantId: Long): Flow<List<PlantLogEntity>> {
        return plantDao.getLogsForPlant(plantId)
    }

    fun updateLog(log: PlantLogEntity) {
        viewModelScope.launch {
            plantDao.insertLog(log)
        }
    }

    fun addLog(
        plantId: Long,
        watered: Boolean,
        status: String,
        note: String? = null,
        heightCm: Float = 0f,
        fertilizerType: String? = null,
        fertilizerDose: String? = null
    ) {
        viewModelScope.launch {
            val log = PlantLogEntity(
                plantId = plantId,
                date = System.currentTimeMillis(),
                watered = watered,
                status = status,
                note = note,
                heightCm = heightCm,
                fertilizerType = fertilizerType,
                fertilizerDose = fertilizerDose
            )
            plantDao.insertLog(log)
        }
    }

    fun addExpense(plantId: Long, amount: Float, note: String?) {
        viewModelScope.launch {
            expenseDao.insert(
                PlantExpenseEntity(
                    plantId = plantId,
                    amount = amount,
                    note = note
                )
            )
        }
    }

    fun getExpensesForPlant(plantId: Long): Flow<List<PlantExpenseEntity>> {
        return expenseDao.getExpensesForPlant(plantId)
    }
}
