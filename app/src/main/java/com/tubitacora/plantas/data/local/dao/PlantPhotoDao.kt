package com.tubitacora.plantas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tubitacora.plantas.data.local.entity.PlantPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantPhotoDao {
    @Insert
    suspend fun insert(photo: PlantPhotoEntity)

    @Query("SELECT * FROM plant_photos WHERE plantId = :plantId ORDER BY timestamp DESC")
    fun getPhotosForPlant(plantId: Long): Flow<List<PlantPhotoEntity>>
}
