package com.tubitacora.plantas.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DayForecastCard(
    title: String,          // "Hoy" | "Mañana"
    shouldWater: Boolean,
    condition: String,
    date: String
) {
    val bgColor =
        if (shouldWater)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.errorContainer

    val textColor =
        if (shouldWater)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onErrorContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = if (shouldWater) "💧" else "🚫",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = if (shouldWater)
                    "Puedes regar la planta 🌱"
                else
                    "Mejor no regar hoy 🌧️",
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Clima: $condition",
                color = textColor,
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "Fecha: $date",
                color = textColor,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
