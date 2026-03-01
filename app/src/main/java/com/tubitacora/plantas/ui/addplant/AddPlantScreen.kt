package com.tubitacora.plantas.ui.addplant

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantScreen(
    onSave: (String, String, Int, Long, String?) -> Unit, // ✅ Añadido Long para la fecha
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var frequencySlider by remember { mutableStateOf(7f) }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    
    // --- Lógica de Fecha ---
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Planta") },
                navigationIcon = { IconButton(onClick = onCancel) { Icon(Icons.Default.Close, "Cancelar") } }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving && name.isNotBlank() && type.isNotBlank(),
                    onClick = {
                        isSaving = true
                        onSave(
                            name,
                            type,
                            frequencySlider.roundToInt(),
                            selectedDate,
                            notes.ifBlank { null }
                        )
                    }
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Guardar Planta")
                    }
                }
                OutlinedButton(modifier = Modifier.weight(1f), onClick = onCancel) {
                    Text("Cancelar")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Datos de la Planta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la planta o lote.") },
                leadingIcon = { Icon(Icons.Default.Title, null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Especie y cantidad (eje:aureca, 100)") },
                leadingIcon = { Icon(Icons.Default.Eco, null) },
                modifier = Modifier.fillMaxWidth()
            )

            // ✅ NUEVO: Selector de Fecha de Siembra
            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Fecha de Siembra", style = MaterialTheme.typography.labelMedium)
                        Text(dateFormat.format(Date(selectedDate)), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Frecuencia de Riego", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Text(
                text = "Regar cada ${frequencySlider.roundToInt()} día(s)",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Slider(
                value = frequencySlider,
                onValueChange = { frequencySlider = it },
                valueRange = 1f..30f,
                steps = 28
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas adicionales (opcional)") },
                leadingIcon = { Icon(Icons.Default.Notes, null) },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        }
    }

    // Diálogo del Selector de Fecha
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
