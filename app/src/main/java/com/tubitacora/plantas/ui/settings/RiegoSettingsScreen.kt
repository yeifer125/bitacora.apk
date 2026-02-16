package com.tubitacora.plantas.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tubitacora.plantas.viewmodel.PlantViewModel
import androidx.compose.runtime.collectAsState
import com.tubitacora.plantas.data.local.entity.PlantEntity
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiegoSettingsScreen(
    plantViewModel: PlantViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val plants by plantViewModel.plants.collectAsState(initial = emptyList())

    val reminderStates = remember { mutableStateMapOf<Long, Boolean>() }
    val customTime = remember { mutableStateMapOf<Long, String>() }
    val useCustomTime = remember { mutableStateMapOf<Long, Boolean>() }


    LaunchedEffect(plants) {
        plants.forEach { plant ->
            if (!reminderStates.containsKey(plant.id)) {
                reminderStates[plant.id] = plant.reminderActive
            }
            if (!useCustomTime.containsKey(plant.id)) {
                useCustomTime[plant.id] = plant.customDelayMillis != null
            }
            if (!customTime.containsKey(plant.id)) {
                val delay = plant.customDelayMillis
                if (delay != null) {
                    val hours = TimeUnit.MILLISECONDS.toHours(delay)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(delay) % 60
                    customTime[plant.id] = String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
                } else {
                    customTime[plant.id] = "00:00"
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de Riego") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(plants, key = { it.id }) { plant ->
                PlantReminderCard(
                    plant = plant,
                    reminderActive = reminderStates[plant.id] ?: false,
                    currentTimeStr = customTime[plant.id] ?: "00:00",
                    useCustomTime = useCustomTime[plant.id] ?: false,
                    onStateChange = { active, timeStr, useCusTime ->
                        reminderStates[plant.id] = active
                        customTime[plant.id] = timeStr
                        useCustomTime[plant.id] = useCusTime

                        val delayMillis = if (useCusTime) {
                            parseTimeToMillis(timeStr)
                        } else {
                            TimeUnit.DAYS.toMillis(plant.wateringFrequencyDays.toLong())
                        }
                        plantViewModel.updateReminderState(plant.id, active, delayMillis)
                    }
                )
            }

            // ✈️ SECCIÓN MODO VACACIONES
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "✈️ Modo Vacaciones",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            "Genera una lista de cuidados para enviarla por WhatsApp a quien cuide tus plantas.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Button(
                            onClick = { shareVacationInstructions(context, plants) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Icon(Icons.Default.FlightTakeoff, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Compartir instrucciones")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantReminderCard(
    plant: PlantEntity,
    reminderActive: Boolean,
    currentTimeStr: String,
    useCustomTime: Boolean,
    onStateChange: (Boolean, String, Boolean) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val timeState = rememberTimePickerState(
        initialHour = currentTimeStr.split(":")[0].toIntOrNull() ?: 0,
        initialMinute = currentTimeStr.split(":")[1].toIntOrNull() ?: 0,
        is24Hour = true
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminderActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(plant.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Frecuencia: ${plant.wateringFrequencyDays} días",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = reminderActive,
                    onCheckedChange = { onStateChange(it, currentTimeStr, useCustomTime) }
                )
            }

            if (reminderActive) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = useCustomTime,
                        onCheckedChange = { onStateChange(reminderActive, currentTimeStr, it) }
                    )
                    Text("Usar tiempo personalizado")
                }

                if (useCustomTime) {
                    OutlinedCard(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recordatorio cada: $currentTimeStr (Horas:Minutos)")
                        }
                    }
                }

                val delayMillis = if (useCustomTime) {
                    parseTimeToMillis(currentTimeStr)
                } else {
                    TimeUnit.DAYS.toMillis(plant.wateringFrequencyDays.toLong())
                }
                CountdownTimer(plant.lastWatered, delayMillis)
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                val newTime = String.format(Locale.getDefault(), "%02d:%02d", timeState.hour, timeState.minute)
                onStateChange(reminderActive, newTime, true)
                showTimePicker = false
            }
        ) {
            TimePicker(state = timeState)
        }
    }
}

@Composable
fun CountdownTimer(lastWatered: Long, delayMillis: Long?) {
    if (delayMillis == null) return
    var timeLeft by remember { mutableStateOf(0L) }

    LaunchedEffect(lastWatered, delayMillis) {
        while (true) {
            val targetTime = lastWatered + delayMillis
            timeLeft = targetTime - System.currentTimeMillis()
            delay(1000)
        }
    }

    if (timeLeft > 0) {
        val hours = timeLeft / (1000 * 60 * 60)
        val minutes = (timeLeft / (1000 * 60)) % 60
        val seconds = (timeLeft / 1000) % 60
        Text(
            text = "⏰ Próximo aviso en: %02d:%02d:%02d".format(hours, minutes, seconds),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )
    } else {
        Text(
            text = "💧 ¡Toca regar!",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onConfirm) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        text = { content() }
    )
}

fun parseTimeToMillis(time: String?): Long? {
    if (time.isNullOrBlank()) return null
    val parts = time.split(":")
    val hours = parts.getOrNull(0)?.toLongOrNull() ?: 0L
    val minutes = parts.getOrNull(1)?.toLongOrNull() ?: 0L
    return (hours * 3600 + minutes * 60) * 1000
}

private fun shareVacationInstructions(context: Context, plants: List<PlantEntity>) {
    val activePlants = plants.filter { it.reminderActive }
    if (activePlants.isEmpty()) return

    val instructions = activePlants.joinToString("\n") { plant ->
        "- ${plant.name}: Regar cada ${plant.wateringFrequencyDays} días."
    }

    val message = "🌿 Instrucciones de mis plantas para mis vacaciones:\n\n$instructions\n\n¡Gracias por cuidarlas! ❤️"
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir instrucciones"))
}
