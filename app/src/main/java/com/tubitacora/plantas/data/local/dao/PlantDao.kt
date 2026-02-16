package com.tubitacora.plantas.data.local.dao

import androidx.room.*
import com.tubitacora.plantas.data.local.entity.PlantEntity
import com.tubitacora.plantas.data.local.entity.PlantLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {

    // ---------- PLANTS ----------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plant: PlantEntity)

    @Update
    suspend fun update(plant: PlantEntity)

    @Delete
    suspend fun delete(plant: PlantEntity)

    @Query("SELECT * FROM plants WHERE id = :plantId LIMIT 1")
    suspend fun getPlantById(plantId: Long): PlantEntity?

    @Query("SELECT * FROM plants ORDER BY plantingDate DESC")
    fun getAllPlants(): Flow<List<PlantEntity>>

    // ---------- TODOS LOS LOGS ----------
    @Query("""
        SELECT * FROM plant_logs
        WHERE plantId = :plantId
        ORDER BY date ASC
    """)
    fun getLogsForPlant(plantId: Long): Flow<List<PlantLogEntity>>

    // ---------- 💧 RIEGO ----------
    @Query("""
        SELECT * FROM plant_logs
        WHERE plantId = :plantId
        AND watered = 1
        ORDER BY date DESC
    """)
    fun getWateringLogsForPlant(plantId: Long): Flow<List<PlantLogEntity>>

    // ---------- 📈 CRECIMIENTO ----------
    @Query("""
        SELECT * FROM plant_logs
        WHERE plantId = :plantId
        AND status = 'GROWTH'
        ORDER BY date ASC
    """)
    fun getGrowthLogsForPlant(plantId: Long): Flow<List<PlantLogEntity>>

    // ---------- 📝 NOTAS ----------
    @Query("""
        SELECT * FROM plant_logs
        WHERE plantId = :plantId
        AND status = 'NOTE'
        ORDER BY date DESC
    """)
    fun getNoteLogsForPlant(plantId: Long): Flow<List<PlantLogEntity>>

    // ---------- INSERT ----------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PlantLogEntity)

}
