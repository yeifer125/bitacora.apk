package com.tubitacora.plantas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tubitacora.plantas.data.local.entity.LogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {

    @Update
    suspend fun updateLog(log: LogEntity)

    @Insert
    suspend fun insert(log: LogEntity)

    @Query("SELECT * FROM logs WHERE plantId = :plantId ORDER BY date DESC")
    fun getLogsForPlant(plantId: Long): Flow<List<LogEntity>>
}
