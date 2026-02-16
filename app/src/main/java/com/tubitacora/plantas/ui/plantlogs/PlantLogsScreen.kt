package com.tubitacora.plantas.ui.plantlogs

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.tubitacora.plantas.data.local.entity.PlantLogEntity
import com.tubitacora.plantas.viewmodel.LogViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantLogsScreen(
    plantId: Long,
    plantName: String,
    logViewModel: LogViewModel,
    onBack: () -> Unit
) {
    val logs by logViewModel
        .getLogsForPlant(plantId)
        .collectAsState(initial = emptyList())

    var showImageUri by remember { mutableStateOf<Uri?>(null) }
    var editingLog by remember { mutableStateOf<PlantLogEntity?>(null) }

    val dateFormat = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registros de $plantName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("⬅️")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(logs) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editingLog = log },
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "${log.status.replaceFirstChar { it.titlecase() }} - ${dateFormat.format(Date(log.date))}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        log.note?.let { note ->
                            if (note.startsWith("content://")) {
                                Image(
                                    painter = rememberAsyncImagePainter(Uri.parse(note)),
                                    contentDescription = "Imagen del registro",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .padding(top = 6.dp)
                                        .clickable { showImageUri = Uri.parse(note) }
                                )
                            } else {
                                Text(
                                    text = "📝 $note",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }

                        if (log.watered) {
                            Text("💧 Riego registrado", modifier = Modifier.padding(top = 4.dp))
                        }

                        if (log.heightCm > 0f) {
                            Text(
                                text = "📈 Crecimiento: ${log.heightCm} cm",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        if (!log.fertilizerType.isNullOrBlank()) {
                             Text(
                                text = "🌱 Abono: ${log.fertilizerType}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            if (!log.fertilizerDose.isNullOrBlank()) {
                                Text(
                                    text = "   Dosis: ${log.fertilizerDose}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ---------- IMAGEN FULL ----------
    if (showImageUri != null) {
        Dialog(onDismissRequest = { showImageUri = null }) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(showImageUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // ---------- EDITAR LOG ----------
    editingLog?.let { log ->
        var editedNote by remember { mutableStateOf(log.note ?: "") }
        var editedWatered by remember { mutableStateOf(log.watered) }

        AlertDialog(
            onDismissRequest = { editingLog = null },
            title = { Text("Editar registro") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedNote,
                        onValueChange = { editedNote = it },
                        label = { Text("Nota") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = editedWatered,
                            onCheckedChange = { editedWatered = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Marcado como regado")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        logViewModel.updateLog(
                            log.copy(
                                note = editedNote.ifBlank { null },
                                watered = editedWatered
                            )
                        )
                        editingLog = null
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { editingLog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
