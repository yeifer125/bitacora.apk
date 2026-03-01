package com.tubitacora.plantas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tubitacora.plantas.data.local.AppDatabase
import com.tubitacora.plantas.data.local.entity.PlantExpenseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val expenseDao = db.plantExpenseDao()

    fun getExpensesForPlant(plantId: Long): Flow<List<PlantExpenseEntity>> {
        return expenseDao.getExpensesForPlant(plantId)
    }

    fun addTransaction(plantId: Long, amount: Float, note: String?, isExpense: Boolean) {
        viewModelScope.launch {
            val expense = PlantExpenseEntity(
                plantId = plantId,
                amount = amount,
                note = note,
                isExpense = isExpense
            )
            expenseDao.insert(expense)
        }
    }

    fun updateTransaction(expense: PlantExpenseEntity) {
        viewModelScope.launch {
            expenseDao.update(expense)
        }
    }

    fun deleteTransaction(expense: PlantExpenseEntity) {
        viewModelScope.launch {
            expenseDao.delete(expense)
        }
    }
}
