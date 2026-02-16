package com.tubitacora.plantas

import android.Manifest
import android.os.Bundle
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tubitacora.plantas.ui.navigation.AppNavHost
import com.tubitacora.plantas.ui.theme.BitacoraPlantasTheme
import com.tubitacora.plantas.viewmodel.PlantViewModel

class MainActivity : ComponentActivity() {

    // Launcher para pedir permiso de notificaciones en Android 13+
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Aquí podrías manejar si el usuario rechazó el permiso
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        checkNotificationPermission()

        setContent {
            val plantViewModel: PlantViewModel = viewModel()
            BitacoraPlantasTheme {
                AppNavHost(plantViewModel = plantViewModel)
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "watering_channel",
                "Recordatorios de riego",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para avisar cuándo toca regar las plantas"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
