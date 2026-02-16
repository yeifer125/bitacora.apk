package com.tubitacora.plantas.ui.IA

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tubitacora.plantas.data.local.entity.PlantEntity
import com.tubitacora.plantas.data.local.entity.PlantLogEntity
import com.tubitacora.plantas.viewmodel.AiViewModel
import com.tubitacora.plantas.viewmodel.LogViewModel
import kotlinx.coroutines.launch

@Composable
fun AiScreen(
    plant: PlantEntity?,
    aiViewModel: AiViewModel = viewModel(),
    logViewModel: LogViewModel = viewModel()
) {
    var userInput by remember { mutableStateOf("") }
    val messages by aiViewModel.messages.collectAsState()
    val isLoading by aiViewModel.isLoading.collectAsState()
    val listState = rememberLazyListState()

    // 1. Cargar los registros de la planta directamente aquí.
    val plantLogs by logViewModel.getLogsForPlant(plant?.id ?: -1).collectAsState(initial = null)

    // 2. Usar un estado para asegurar que la recomendación se envíe solo una vez.
    var recommendationSent by remember { mutableStateOf(false) }

    // 3. El LaunchedEffect se activa cuando los `plantLogs` se terminan de cargar (dejan de ser null).
    LaunchedEffect(plantLogs) {
        if (plant != null && plantLogs != null && !recommendationSent) {
            aiViewModel.sendPlantRecommendation(plant, plantLogs!!)
            recommendationSent = true
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message -> ChatBubble(message) }
            if (isLoading) {
                item { TypingIndicator() }
            }
        }

        Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                BasicTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier.weight(1f),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            if (userInput.isEmpty()) {
                                Text("Pregunta algo...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            innerTextField()
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (userInput.isNotBlank()) {
                            aiViewModel.sendPrompt(userInput)
                            userInput = ""
                        }
                    },
                    enabled = userInput.isNotBlank() && !isLoading,
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(message: ChatMessage) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isAi) Arrangement.Start else Arrangement.End
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(
                    color = if (message.isAi) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (message.isAi) 0.dp else 16.dp,
                        bottomEnd = if (message.isAi) 16.dp else 0.dp
                    )
                )
                .clip(RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp,
                    bottomStart = if (message.isAi) 0.dp else 16.dp,
                    bottomEnd = if (message.isAi) 16.dp else 0.dp
                ))
                .combinedClickable(
                    onClick = { /* Nada */ },
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString(message.text))
                        Toast.makeText(context, "Texto copiado", Toast.LENGTH_SHORT).show()
                    }
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (message.isAi) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    val dotSize = 8.dp
    val infiniteTransition = rememberInfiniteTransition(label = "")

    @Composable
    fun AnimateDot(delay: Int) = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0f at delay with LinearEasing
                1f at (delay + 300) with LinearEasing
                0f at (delay + 600)
            }
        ),
        label = ""
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        (0..2).forEach { i ->
            val alpha by AnimateDot(delay = i * 200)
            Spacer(
                Modifier
                    .size(dotSize)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                        shape = CircleShape
                    )
            )
        }
    }
}
