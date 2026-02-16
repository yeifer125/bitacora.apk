@file:OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)

package com.tubitacora.plantas.ui.home

import android.widget.Toast
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.tubitacora.plantas.data.local.entity.PlantEntity
import com.tubitacora.plantas.ui.theme.MyGreen
import com.tubitacora.plantas.ui.theme.MyOrange
import com.tubitacora.plantas.ui.theme.MyRed
import com.tubitacora.plantas.viewmodel.PlantPhotoViewModel
import java.util.concurrent.TimeUnit




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    plants: List<PlantEntity>,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onAddClick: () -> Unit,
    onPlantClick: (Long, String, Long, String?) -> Unit,
    onSettingsClick: () -> Unit,
    onWeatherClick: () -> Unit,
    onDeletePlant: (PlantEntity) -> Unit,
    onWaterPlant: (Long) -> Unit,
    onNavigateToStats: (Long) -> Unit,
    onNavigateToAi: (PlantEntity) -> Unit // ✅ Callback para IA
) {
    var plantToDelete by remember { mutableStateOf<PlantEntity?>(null) }

    plantToDelete?.let {
        AlertDialog(
            onDismissRequest = { plantToDelete = null },
            title = { Text("Eliminar Planta") },
            text = { Text("¿Estás seguro de que quieres eliminar ${it.name}?") },
            confirmButton = { Button(onClick = { onDeletePlant(it); plantToDelete = null }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { plantToDelete = null }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("\uD835\uDCDC\uD835\uDCF2\uD835\uDCFC \uD835\uDCD2\uD835\uDCFE\uD835\uDCF5\uD835\uDCFD\uD835\uDCF2\uD835\uDCFF\uD835\uDCF8\uD835\uDCFC", style = LocalTextStyle.current.copy(shadow = Shadow(Color.Black.copy(alpha = 0.3f), offset = Offset(2f, 4f), blurRadius = 8f))) },
                actions = {
                    IconButton(onClick = onWeatherClick) { Icon(Icons.Outlined.WbSunny, "Clima") }
                    IconButton(onClick = onSettingsClick) { Icon(Icons.Outlined.Settings, "Riego") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.glassmorphic()
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick, shape = CircleShape) {
                Icon(Icons.Default.Add, contentDescription = "Añadir planta")
            }
        }
    ) { padding ->
        if (plants.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Añade tu primera planta para empezar", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            val photoViewModel: PlantPhotoViewModel = viewModel()
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 180.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding() + 16.dp,
                    bottom = padding.calculateBottomPadding() + 80.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(plants, key = { it.id }) { plant ->
                    PlantCardWithPhotoLoader(
                        plant = plant,
                        photoViewModel = photoViewModel,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedContentScope = animatedContentScope,
                        onWaterClick = { onWaterPlant(plant.id) },
                        onClick = { photoUri -> onPlantClick(plant.id, plant.name, plant.plantingDate, photoUri) },
                        onLongClick = { plantToDelete = plant },
                        onNavigateToStats = { onNavigateToStats(plant.id) },
                        onNavigateToAi = { onNavigateToAi(plant) } // Pasar el callback
                    )
                }
            }
        }
    }
}

@Composable
fun Modifier.glassmorphic(): Modifier {
    return this.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
}

@Composable
private fun PlantCardWithPhotoLoader(
    plant: PlantEntity,
    photoViewModel: PlantPhotoViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onClick: (String?) -> Unit,
    onLongClick: () -> Unit,
    onWaterClick: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToAi: () -> Unit
) {
    val photos by photoViewModel.getPhotosForPlant(plant.id).collectAsState(initial = emptyList())
    val firstPhotoUri = photos.firstOrNull()?.uri

    PlantCard(
        plant = plant,
        photoUri = firstPhotoUri,
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope,
        onClick = { onClick(firstPhotoUri) },
        onLongClick = onLongClick,
        onWaterClick = onWaterClick,
        onNavigateToStats = onNavigateToStats,
        onNavigateToAi = onNavigateToAi
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PlantCard(
    plant: PlantEntity,
    photoUri: String?,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onWaterClick: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val timeSinceWatered = System.currentTimeMillis() - plant.lastWatered
    val wateringInterval = TimeUnit.DAYS.toMillis(plant.wateringFrequencyDays.toLong().coerceAtLeast(1))
    val waterProgress = (timeSinceWatered.toFloat() / wateringInterval.toFloat()).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(targetValue = waterProgress, label = "WaterProgressAnimation")
    val needsWatering = waterProgress > 0.9f

    with(sharedTransitionScope) {
        Card(
            modifier = modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center) {
                    WateringRing(progress = animatedProgress)
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .sharedElement(rememberSharedContentState(key = "image-${plant.id}"), animatedVisibilityScope = animatedContentScope),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri != null) {
                            Image(painter = rememberAsyncImagePainter(photoUri), contentDescription = plant.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(imageVector = Icons.Default.Yard, contentDescription = plant.name, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (needsWatering) {
                        StatusBadge(modifier = Modifier.align(Alignment.TopEnd))
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = plant.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = plant.type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        IconButton(onClick = {
                            onWaterClick()
                            Toast.makeText(context, "¡${plant.name} regada!", Toast.LENGTH_SHORT).show()
                        }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Outlined.WaterDrop, "Regar", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onNavigateToStats, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.BarChart, "Estadísticas", tint = MaterialTheme.colorScheme.primary)
                        }
                        // ✅ BOTÓN DE IA AÑADIDO
                        IconButton(onClick = { onNavigateToAi() }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.AutoAwesome, "Chat IA", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WateringRing(progress: Float) {
    val ringColor = when {
        progress < 0.5f -> MyGreen
        progress < 0.85f -> MyOrange
        else -> MyRed
    }

    CircularProgressIndicator(
        progress = { 1 - progress },
        modifier = Modifier.size(74.dp),
        color = ringColor,
        strokeWidth = 4.dp,
        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        strokeCap = StrokeCap.Round
    )
}

@Composable
fun StatusBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.offset(x = 4.dp, y = (-4).dp),
        shape = CircleShape,
        color = MyRed,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Necesita riego",
            tint = Color.White,
            modifier = Modifier.padding(3.dp).size(10.dp)
        )
    }
}
