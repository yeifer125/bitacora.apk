package com.tubitacora.plantas.ui.plantdetail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.tubitacora.plantas.data.local.entity.PlantLogEntity
import com.tubitacora.plantas.data.local.entity.PlantPhotoEntity
import com.tubitacora.plantas.ui.theme.MyGreen
import com.tubitacora.plantas.ui.theme.Purple40
import com.tubitacora.plantas.viewmodel.LogViewModel
import com.tubitacora.plantas.viewmodel.PlantPhotoViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private val HeaderHeight = 300.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PlantDetailScreen(
    plantId: Long,
    plantName: String,
    plantingDate: Long,
    initialPhotoUri: String?,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    logViewModel: LogViewModel,
    photoViewModel: PlantPhotoViewModel,
    onBack: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToAi: () -> Unit
) {
    val context = LocalContext.current
    val logs by logViewModel.getLogsForPlant(plantId).collectAsState(initial = emptyList())
    val photos by photoViewModel.getPhotosForPlant(plantId).collectAsState(initial = emptyList())
    val firstPhotoUri = photos.firstOrNull()?.uri ?: initialPhotoUri

    val lazyListState = rememberLazyListState()

    val headerHeightPx = with(LocalDensity.current) { HeaderHeight.toPx() }
    val toolbarHeightPx = with(LocalDensity.current) { 56.dp.toPx() }

    val toolbarAlpha = remember {
        derivedStateOf { calculateToolbarAlpha(lazyListState, headerHeightPx, toolbarHeightPx) }
    }

    with(sharedTransitionScope) {
        Box(modifier = Modifier.fillMaxSize()) {
            CollapsingHeader(firstPhotoUri, plantId, animatedContentScope, lazyListState)

            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item { Spacer(modifier = Modifier.height(HeaderHeight)) }
                item { PlantInfoCard(plantName, plantingDate, logs, context, onNavigateToAi, onNavigateToExpenses, onNavigateToLogs) }
                item { PhotoSection(plantId, photos, photoViewModel) }
                item { NewLogSection(plantId, logViewModel) }

                if (logs.isNotEmpty()) {
                    item {
                        Text(
                            "Historial de Registros",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(logs, key = { it.id }) {
                        LogItemCard(log = it)
                    }
                }
            }

            CollapsingToolbar(plantName, onBack, toolbarAlpha.value)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollapsingToolbar(title: String, onBack: () -> Unit, alpha: Float) {
    TopAppBar(
        title = { Text(title, modifier = Modifier.graphicsLayer { this.alpha = alpha }) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MyGreen.copy(alpha = alpha),
            titleContentColor = Color.White
        ),
        modifier = Modifier.background(
            Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent))
        )
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.CollapsingHeader(
    photoUri: String?,
    plantId: Long,
    animatedContentScope: AnimatedContentScope,
    scrollState: androidx.compose.foundation.lazy.LazyListState
) {
    val scrollOffset = scrollState.firstVisibleItemScrollOffset.toFloat()
    val firstItemIndex = scrollState.firstVisibleItemIndex
    val headerHeightPx = with(LocalDensity.current) { HeaderHeight.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeaderHeight)
            .graphicsLayer {
                if (firstItemIndex == 0) {
                    translationY = scrollOffset * 0.5f
                    alpha = 1f - (scrollOffset / headerHeightPx).coerceIn(0f, 1f)
                }
            }
            .sharedElement(
                rememberSharedContentState(key = "image-$plantId"),
                animatedVisibilityScope = animatedContentScope
            ),
        contentAlignment = Alignment.Center
    ) {
        if (photoUri != null) {
            Image(painter = rememberAsyncImagePainter(photoUri), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MyGreen, Purple40))))
        }
    }
}

@Composable
fun PlantInfoCard(
    plantName: String,
    plantingDate: Long,
    logs: List<PlantLogEntity>,
    context: Context,
    onNavigateToAi: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToLogs: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    Surface(
        modifier = Modifier.fillMaxWidth().offset(y = (-24).dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = plantName, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(text = "Sembrada el ${dateFormat.format(Date(plantingDate))}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                ActionButton(icon = Icons.Default.AutoAwesome, label = "IA", onClick = onNavigateToAi)
                ActionButton(icon = Icons.Default.PictureAsPdf, label = "PDF", onClick = { exportToHealthReport(context, plantName, plantingDate, logs) })
                ActionButton(icon = Icons.Default.Payments, label = "Gastos", onClick = onNavigateToExpenses)
                ActionButton(icon = Icons.Default.History, label = "Logs", onClick = onNavigateToLogs)
            }
        }
    }
}

@Composable
private fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp) 
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PhotoSection(plantId: Long, photos: List<PlantPhotoEntity>, photoViewModel: PlantPhotoViewModel) {
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it && tempPhotoUri != null) photoViewModel.addPhoto(plantId, tempPhotoUri.toString())
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Galería de Fotos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {
                createImageUri(context)?.let { uri -> tempPhotoUri = uri; cameraLauncher.launch(uri) }
            }) { Icon(Icons.Default.CameraAlt, "Tomar foto") }
        }
        if (photos.isEmpty()) {
            Text("Aún no has añadido fotos.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                items(photos) { photo ->
                    var showDialog by remember { mutableStateOf(false) }
                    Image(painter = rememberAsyncImagePainter(photo.uri), null, Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)).clickable { showDialog = true }, contentScale = ContentScale.Crop)
                    if (showDialog) Dialog(onDismissRequest = { showDialog = false }) { Image(rememberAsyncImagePainter(photo.uri), null) }
                }
            }
        }
    }
}

@Composable
fun NewLogSection(plantId: Long, logViewModel: LogViewModel) {
    var logNote by remember { mutableStateOf("") }
    var logWatered by remember { mutableStateOf(false) }
    var logGrowth by remember { mutableStateOf(false) }
    var growthValue by remember { mutableStateOf("") }
    var logFertilizer by remember { mutableStateOf(false) }
    var fertilizerType by remember { mutableStateOf("") }
    var fertilizerDose by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        FilledTonalButton(
            onClick = { expanded = !expanded }, 
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Icon(if (expanded) Icons.Default.Close else Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Añadir Nuevo Registro")
        }
        if (expanded) {
            // ✅ Envoltura con Card para mejor visibilidad y diseño
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = logNote, onValueChange = { logNote = it }, label = { Text("Nota / Observación") }, modifier = Modifier.fillMaxWidth())
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { logWatered = !logWatered }) {
                        Checkbox(checked = logWatered, onCheckedChange = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Regada") // Color heredado de la Card
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { logGrowth = !logGrowth }) {
                        Checkbox(checked = logGrowth, onCheckedChange = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Crecimiento") // Color heredado de la Card
                    }
                    if (logGrowth) {
                        OutlinedTextField(value = growthValue, onValueChange = { growthValue = it }, label = { Text("Altura (cm)") }, modifier = Modifier.fillMaxWidth())
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { logFertilizer = !logFertilizer }) {
                        Checkbox(checked = logFertilizer, onCheckedChange = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Abono") // Color heredado de la Card
                    }
                    if (logFertilizer) {
                        OutlinedTextField(value = fertilizerType, onValueChange = { fertilizerType = it }, label = { Text("Tipo de Abono") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = fertilizerDose, onValueChange = { fertilizerDose = it }, label = { Text("Dosis") }, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = {
                        logViewModel.addLog(
                            plantId,
                            logWatered,
                            "NOTE",
                            logNote.ifBlank { null },
                            growthValue.toFloatOrNull() ?: 0f,
                            if (logFertilizer) fertilizerType.ifBlank { null } else null,
                            if (logFertilizer) fertilizerDose.ifBlank { null } else null
                        )
                        expanded = false
                    }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar Registro")
                    }
                }
            }
        }
    }
}

@Composable
fun LogItemCard(log: PlantLogEntity) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(dateFormat.format(Date(log.date)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            val details = mutableListOf<String>()
            if (log.watered) details.add("💧 Riego")
            if (log.heightCm > 0f) details.add("📈 Crecimiento: ${log.heightCm} cm")
            if (!log.fertilizerType.isNullOrBlank()) details.add("🌱 Abono: ${log.fertilizerType} (${log.fertilizerDose})")
            if (!log.note.isNullOrBlank()) details.add("📝 Nota: ${log.note}")

            if (details.isEmpty()) Text("Sin detalles en este registro.")
            else Text(details.joinToString(" • "), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun calculateToolbarAlpha(
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    headerHeightPx: Float,
    toolbarHeightPx: Float
): Float {
    return if (scrollState.firstVisibleItemIndex > 0) 1f
    else (scrollState.firstVisibleItemScrollOffset / (headerHeightPx - toolbarHeightPx)).coerceIn(0f, 1f)
}

private fun createImageUri(context: Context): Uri? {
    val file = File.createTempFile("plant_photo_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

private fun exportToHealthReport(context: Context, plantName: String, plantingDate: Long, logs: List<PlantLogEntity>) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val report = StringBuilder().apply {
        append("📄 INFORME DE CULTIVO: $plantName\n")
        append("-----------------------------------\n")
        append("Fecha de siembra: ${dateFormat.format(Date(plantingDate))}\n\n")
        append("--- Historial de Bitácora ---\n\n")

        if (logs.isEmpty()) {
            append("No hay registros en la bitácora para esta planta.\n")
        } else {
            logs.sortedBy { it.date }.forEach { log ->
                append("• ${dateFormat.format(Date(log.date))}: ")
                val details = mutableListOf<String>()
                if (log.watered) details.add("💧 Riego")
                if (log.heightCm > 0f) details.add("📈 Crecimiento: ${log.heightCm} cm")
                if (!log.fertilizerType.isNullOrBlank()) {
                    var fertilizerDetail = "🌱 Abono: ${log.fertilizerType}"
                    if (!log.fertilizerDose.isNullOrBlank()) {
                        fertilizerDetail += " (${log.fertilizerDose})"
                    }
                    details.add(fertilizerDetail)
                }
                if (!log.note.isNullOrBlank()) details.add("📝 Nota: '${log.note}'")

                if (details.isNotEmpty()) {
                    append(details.joinToString(" - "))
                } else {
                    append("Registro sin detalles.")
                }
                append("\n")
            }
        }
        append("\n¡Gracias por cuidar de tus plantas! 💚")
    }.toString()

    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, report)
        putExtra(Intent.EXTRA_SUBJECT, "Informe de Cultivo: $plantName")
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Exportar Informe de Cultivo")
    context.startActivity(shareIntent)
}
