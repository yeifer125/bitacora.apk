package com.tubitacora.plantas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tubitacora.plantas.data.local.entity.WeatherDecisionEntity

@Dao
interface WeatherDecisionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(decision: WeatherDecisionEntity)

    // Solo para hoy
    @Query("SELECT * FROM weather_decisions WHERE date = :today")
    suspend fun getAllForToday(today: String = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())): List<WeatherDecisionEntity>

    // ✅ Historial completo
    @Query("SELECT * FROM weather_decisions ORDER BY date DESC")
    suspend fun getAll(): List<WeatherDecisionEntity>

    // Historial filtrado por planta
    @Query("SELECT * FROM weather_decisions WHERE plantId = :plantId ORDER BY date DESC")
    suspend fun getByPlant(plantId: Long): List<WeatherDecisionEntity>

    @Query("SELECT * FROM weather_decisions WHERE plantId = :plantId ORDER BY date ASC")
    fun getAllForPlant(plantId: Long): kotlinx.coroutines.flow.Flow<List<WeatherDecisionEntity>>


    // Buscar decisión de un día específico
    @Query("SELECT * FROM weather_decisions WHERE plantId = :plantId AND date = :date LIMIT 1")
    suspend fun getDecisionForDay(plantId: Long, date: String): WeatherDecisionEntity?
}
