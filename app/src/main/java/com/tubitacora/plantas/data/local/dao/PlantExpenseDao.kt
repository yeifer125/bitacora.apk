package com.tubitacora.plantas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tubitacora.plantas.data.local.entity.PlantExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantExpenseDao {
    @Insert
    suspend fun insert(expense: PlantExpenseEntity)

    @Query("SELECT * FROM PlantExpenseEntity WHERE plantId = :plantId")
    fun getExpensesForPlant(plantId: Long): Flow<List<PlantExpenseEntity>>
}
