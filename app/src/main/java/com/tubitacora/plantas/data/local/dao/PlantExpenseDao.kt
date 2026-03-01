package com.tubitacora.plantas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tubitacora.plantas.data.local.entity.PlantExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantExpenseDao {
    @Insert
    suspend fun insert(expense: PlantExpenseEntity)

    @Update
    suspend fun update(expense: PlantExpenseEntity)

    @Delete
    suspend fun delete(expense: PlantExpenseEntity)

    @Query("SELECT * FROM PlantExpenseEntity WHERE plantId = :plantId ORDER BY date DESC")
    fun getExpensesForPlant(plantId: Long): Flow<List<PlantExpenseEntity>>
}
