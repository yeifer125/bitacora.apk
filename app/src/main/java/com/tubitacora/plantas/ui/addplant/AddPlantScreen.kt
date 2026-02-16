package com.tubitacora.plantas.ui.addplant

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantScreen(
    onSave: (String, String, Int, String?) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var frequencySlider by remember { mutableStateOf(7f) }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

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

            Spacer(modifier = Modifier.height(16.dp))

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
                steps = 28 // 30-1-1 = 28
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
}
