package com.tubitacora.plantas.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tubitacora.plantas.data.local.AppDatabase
import com.tubitacora.plantas.data.local.entity.PlantPhotoEntity
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PlantPhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val photoDao =
        AppDatabase.getDatabase(application).plantPhotoDao()

    fun getPhotosForPlant(plantId: Long) =
        photoDao.getPhotosForPlant(plantId)

    fun addPhoto(plantId: Long, uri: String) {
        viewModelScope.launch {
            photoDao.insert(
                PlantPhotoEntity(
                    plantId = plantId,
                    uri = uri,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // ✅ ESTA ES LA FUNCIÓN QUE TE FALTABA
    fun createImageUri(context: Context): Uri {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())

        val imageFile = File(
            context.cacheDir,
            "plant_$timeStamp.jpg"
        )

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    }
}
