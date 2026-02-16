package com.tubitacora.plantas.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tubitacora.plantas.R

class WaterReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {

        // Obtenemos el nombre de la planta desde los datos de entrada
        val plantName = inputData.getString("plantName") ?: return Result.success()

        // Creamos la notificación
        val notification = NotificationCompat.Builder(applicationContext, "watering_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💧 Hora de regar")
            .setContentText("Es momento de regar $plantName 🌱")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(plantName.hashCode(), notification)

        return Result.success()
    }
}
