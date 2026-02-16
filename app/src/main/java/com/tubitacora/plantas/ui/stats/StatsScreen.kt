package com.tubitacora.plantas.ui.stats

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tubitacora.plantas.data.local.entity.PlantLogEntity
import com.tubitacora.plantas.ui.theme.MyGreen
import com.tubitacora.plantas.viewmodel.PlantStatsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StatsScreen(
    plantId: Long,
    onBack: () -> Unit,
    viewModel: PlantStatsViewModel = viewModel()
) {
    LaunchedEffect(plantId) {
        viewModel.loadStats(plantId)
    }

    val state by viewModel.uiState.collectAsState()

    // ✅ LÓGICA DE TEXTO CORREGIDA Y MEJORADA
    val daysSincePlantingText = when (val days = state.daysSincePlanting) {
        0 -> "Hoy"
        1 -> "1 día"
        else -> "$days días"
    }

    val lastWateredText = when (val days = state.lastWateredDaysAgo) {
        -1 -> "Nunca"
        0 -> "Hoy"
        1 -> "Ayer"
        else -> "Hace $days días"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas de ${state.plantName}") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            GrowthChartCard(growthHistory = state.growthHistory)

            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                maxItemsInEachRow = 2
            ) {
                StatInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Favorite,
                    value = state.healthStatus,
                    label = "Estado de Salud"
                )
                StatInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Schedule,
                    value = daysSincePlantingText, // ✅ TEXTO CORREGIDO
                    label = "Desde Siembra"
                )
                StatInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.WaterDrop,
                    value = "${state.totalWaterings} veces",
                    label = "Riegos Totales"
                )
                StatInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Cloud,
                    value = "${state.wateringAvoided} veces",
                    label = "Riego Ahorrado"
                )
                StatInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AddChart,
                    value = "${state.totalGrowthLogs}",
                    label = "Registros Crecimiento"
                )
                StatInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.TrendingUp,
                    value = "${state.currentHeightCm} cm",
                    label = "Altura Actual"
                )
                StatInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Event,
                    value = lastWateredText, // ✅ TEXTO CORREGIDO
                    label = "Último Riego"
                )
                StatInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Repeat,
                    value = "Cada ${state.wateringFrequency} días",
                    label = "Frecuencia de Riego"
                )
                StatInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CloudQueue,
                    value = "${state.rainDetected} veces",
                    label = "Lluvia Detectada"
                )
            }
        }
    }
}

@Composable
fun StatInfoCard(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Card(modifier = modifier.padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun GrowthChartCard(growthHistory: List<PlantLogEntity>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📈 Curva de Crecimiento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Evolución de la altura (cm). Mantén pulsado para ver detalles.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))

            if (growthHistory.size < 2) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Se necesitan al menos 2 registros de altura para mostrar el gráfico.")
                }
            } else {
                LineChart(data = growthHistory, modifier = Modifier.fillMaxWidth().height(200.dp))
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun LineChart(data: List<PlantLogEntity>, modifier: Modifier = Modifier) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yy", Locale.getDefault()) }
    val animationProgress = remember { Animatable(0f) }
    var touchX by remember { mutableStateOf<Float?>(null) }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(data) {
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 1500))
    }

    // ✅ LÓGICA DE GRÁFICO MÁS ROBUSTA
    val (minTimestamp, maxTimestamp) = remember(data) {
        if (data.size < 2) return@remember 0L to 0L
        data.minOf { it.date } to data.maxOf { it.date }
    }
    val (minHeight, maxHeight) = remember(data) {
        if (data.isEmpty()) return@remember 0f to 0f
        0f to data.maxOf { it.heightCm }
    }

    Box(modifier = modifier
        .pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragStart = { offset -> touchX = offset.x },
                onHorizontalDrag = { change, _ -> touchX = change.position.x },
                onDragEnd = { touchX = null }
            )
        }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            if (data.size < 2) return@Canvas

            fun getX(timestamp: Long) = if (maxTimestamp > minTimestamp) ((timestamp - minTimestamp).toFloat() / (maxTimestamp - minTimestamp).toFloat()) * canvasWidth else 0f
            fun getY(height: Float) = if (maxHeight > minHeight) canvasHeight - ((height - minHeight) / (maxHeight - minHeight)) * canvasHeight else canvasHeight / 2f

            val linePath = Path().apply {
                data.forEachIndexed { i, log ->
                    val x = getX(log.date)
                    val y = getY(log.heightCm)
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
            }

            val animatedPath = Path()
            PathMeasure().apply {
                setPath(linePath, false)
                getSegment(0f, length * animationProgress.value, animatedPath, true)
            }

            val gradientPath = Path().apply {
                addPath(animatedPath)
                data.lastOrNull()?.let { lineTo(getX(it.date), canvasHeight) }
                data.firstOrNull()?.let { lineTo(getX(it.date), canvasHeight) }
                close()
            }

            drawPath(path = gradientPath, brush = Brush.verticalGradient(listOf(MyGreen.copy(alpha = 0.4f), Color.Transparent)))
            drawPath(path = animatedPath, color = MyGreen, style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))

            touchX?.let { currentX ->
                val closestDataPoint = data.minByOrNull { kotlin.math.abs(getX(it.date) - currentX) }
                closestDataPoint?.let { point ->
                    val x = getX(point.date)
                    val y = getY(point.heightCm)

                    drawLine(color = MyGreen.copy(alpha = 0.8f), start = Offset(x, 0f), end = Offset(x, canvasHeight), strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                    drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(x, y))
                    drawCircle(color = MyGreen, radius = 4.dp.toPx(), center = Offset(x, y))

                    val text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp)) {
                            append("${point.heightCm} cm\n")
                        }
                        withStyle(style = SpanStyle(fontSize = 12.sp, color = Color.Gray)) {
                            append(dateFormat.format(Date(point.date)))
                        }
                    }
                    val textLayoutResult = textMeasurer.measure(text)
                    val textX = (x - textLayoutResult.size.width / 2).coerceIn(0f, canvasWidth - textLayoutResult.size.width)
                    val textY = (y - textLayoutResult.size.height - 12.dp.toPx()).coerceAtLeast(0f)

                    drawRect(color = Color.White.copy(alpha = 0.8f), topLeft = Offset(textX - 4.dp.toPx(), textY - 4.dp.toPx()), size = androidx.compose.ui.geometry.Size(textLayoutResult.size.width + 8.dp.toPx(), textLayoutResult.size.height + 8.dp.toPx()))
                    drawText(textLayoutResult, topLeft = Offset(textX, textY))
                }
            }
        }
    }
}
