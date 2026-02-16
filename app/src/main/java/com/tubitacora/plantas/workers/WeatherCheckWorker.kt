package com.tubitacora.plantas.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tubitacora.plantas.R
import com.tubitacora.plantas.data.local.AppDatabase
import com.tubitacora.plantas.data.local.entity.WeatherDecisionEntity
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class WeatherCheckWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    private val client = OkHttpClient()

    override fun doWork(): Result {
        val plantId = inputData.getLong("plantId", -1)
        val plantName = inputData.getString("plantName") ?: "tu planta"

        if (plantId == -1L) return Result.failure()

        val apiKey = "36dafa6e0a0d4e81acf30154260302"
        val lat = "10.366656412175665"
        val lon = "-84.57722658815378"

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.weatherDecisionDao()

        return try {
            val url = "https://api.weatherapi.com/v1/forecast.json?key=$apiKey&q=$lat,$lon&days=2&lang=es"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return Result.failure()

            val json = JSONObject(body)
            val forecastDays = json.getJSONObject("forecast").getJSONArray("forecastday")

            for (i in 0 until forecastDays.length()) {
                val dayJson = forecastDays.getJSONObject(i)
                val date = dayJson.getString("date")

                val existing = runBlocking { dao.getDecisionForDay(plantId, date) }
                if (existing != null && i != 0) continue 

                val dayData = dayJson.getJSONObject("day")
                val conditionText = dayData.getJSONObject("condition").getString("text")
                val maxTemp = dayData.getDouble("maxtemp_c")
                val minTemp = dayData.getDouble("mintemp_c")

                // ❄️ Detectar Heladas o ☀️ Olas de Calor
                if (i == 0) {
                    checkExtremeWeather(plantName, maxTemp, minTemp)
                }

                val shouldWater = !conditionText.contains("lluvia", true) &&
                        !conditionText.contains("llovizna", true) &&
                        !conditionText.contains("chubasco", true)

                runBlocking {
                    dao.insert(
                        WeatherDecisionEntity(
                            plantId = plantId,
                            date = date,
                            shouldWater = shouldWater,
                            conditionText = conditionText
                        )
                    )
                }

                if (i == 0 && shouldWater) {
                    sendNotification("Riego inteligente 🌦️", "Hoy es buen día para regar $plantName 🌱", plantId.toInt())
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun checkExtremeWeather(plantName: String, max: Double, min: Double) {
        if (min < 10) {
            sendNotification("⚠️ Alerta de Frío", "¡Cuidado! Se espera una noche fría ($min°C). Protege tu $plantName ❄️", 999)
        } else if (max > 32) {
            sendNotification("⚠️ Alerta de Calor", "¡Ola de calor detectada! ($max°C). Asegúrate de que $plantName tenga sombra ☀️", 888)
        }
    }

    private fun sendNotification(title: String, text: String, id: Int) {
        val notification = NotificationCompat.Builder(applicationContext, "watering_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(id, notification)
    }
}
