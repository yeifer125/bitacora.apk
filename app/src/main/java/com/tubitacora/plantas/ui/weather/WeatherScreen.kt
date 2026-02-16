package com.tubitacora.plantas.ui.weather

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.*
import com.tubitacora.plantas.data.local.AppDatabase
import com.tubitacora.plantas.workers.WeatherCheckWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/* ---------- UI MODELO ---------- */
data class WeatherUiModel(
    val plantId: Long,
    val shouldWater: Boolean,
    val condition: String,
    val date: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    var decisions by remember { mutableStateOf<List<WeatherUiModel>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var smartWatering by remember {
        mutableStateOf(prefs.getBoolean("smart_watering", true))
    }

    val scope = rememberCoroutineScope()

    fun saveSmartWatering(value: Boolean) {
        prefs.edit().putBoolean("smart_watering", value).apply()
        smartWatering = value

        scope.launch {
            scheduleSmartWatering(context, value)
        }
    }

    /* ---------- CARGA DECISIONES ---------- */
    LaunchedEffect(Unit) {
        loading = true
        decisions = withContext(Dispatchers.IO) {
            db.weatherDecisionDao()
                .getAll()
                .map {
                    WeatherUiModel(
                        plantId = it.plantId,
                        shouldWater = it.shouldWater,
                        condition = it.conditionText,
                        date = it.date
                    )
                }
        }
        loading = false
    }

    val todayDecision = decisions.getOrNull(0)
    val tomorrowDecision = decisions.getOrNull(1)
    val historyDecisions = decisions.drop(2)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🌦️ Clima inteligente") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("⬅️")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            /* ---------- PRONÓSTICO ---------- */
            if (todayDecision != null || tomorrowDecision != null) {

                Text(
                    "Pronóstico para tus plantas",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(8.dp))

                todayDecision?.let {
                    DayForecastCard(
                        title = "Mañana",
                        shouldWater = it.shouldWater,
                        condition = it.condition,
                        date = it.date
                    )
                }

                tomorrowDecision?.let {
                    DayForecastCard(
                        title = "Hoy",
                        shouldWater = it.shouldWater,
                        condition = it.condition,
                        date = it.date
                    )
                }

                Spacer(Modifier.height(16.dp))
            }

            /* ---------- SWITCH ---------- */
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Usar riego inteligente",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = smartWatering,
                        onCheckedChange = { saveSmartWatering(it) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            /* ---------- BOTÓN MANUAL ---------- */
            Button(
                onClick = {
                    scope.launch {
                        val plants = withContext(Dispatchers.IO) {
                            db.plantDao().getAllPlants().first()
                        }

                        plants.forEach { plant ->
                            val work = OneTimeWorkRequestBuilder<WeatherCheckWorker>()
                                .setInputData(
                                    workDataOf(
                                        "plantId" to plant.id,
                                        "plantName" to plant.name
                                    )
                                )
                                .build()

                            WorkManager.getInstance(context).enqueue(work)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("▶ Ejecutar chequeo climático manual")
            }

            Spacer(Modifier.height(20.dp))

            /* ---------- HISTORIAL ---------- */
            Text(
                "Historial de decisiones",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                historyDecisions.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay historial aún 🌤️")
                    }
                }

                else -> {
                    LazyColumn {
                        items(historyDecisions) { decision ->
                            WeatherDecisionCard(decision)
                        }
                    }
                }
            }
        }
    }
}

/* ---------- CARD HISTORIAL ---------- */
@Composable
fun WeatherDecisionCard(decision: WeatherUiModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = if (decision.shouldWater)
                    "🌱 Se recomendó regar"
                else
                    "🌧️ No se recomendó riego",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(4.dp))

            Text("Clima: ${decision.condition}")
            Text("Fecha: ${decision.date}")
        }
    }
}

/* ---------- RIEGO INTELIGENTE ---------- */
suspend fun scheduleSmartWatering(context: Context, enabled: Boolean) {
    val workManager = WorkManager.getInstance(context)
    val db = AppDatabase.getDatabase(context)

    if (!enabled) {
        workManager.cancelAllWorkByTag("smart_watering_worker")
        return
    }

    val plants = withContext(Dispatchers.IO) {
        db.plantDao().getAllPlants().first()
    }

    plants.forEach { plant ->
        val work = PeriodicWorkRequestBuilder<WeatherCheckWorker>(1, TimeUnit.DAYS)
            .setInputData(
                workDataOf(
                    "plantId" to plant.id,
                    "plantName" to plant.name
                )
            )
            .addTag("smart_watering_worker")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "smart_watering_worker_${plant.id}",
            ExistingPeriodicWorkPolicy.REPLACE,
            work
        )
    }
}
