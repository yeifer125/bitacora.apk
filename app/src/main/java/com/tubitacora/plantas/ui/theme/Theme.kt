package com.tubitacora.plantas.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 🛠️ TEMA PROFESIONAL: CYBER-FOREST
 * Aplicando la estética Hacker-Activista a toda la aplicación.
 */

private val DarkColorScheme = darkColorScheme(
    primary = GreenAccent,         // El verde neón como color principal
    onPrimary = Color.Black,       // Texto negro sobre botones verdes neón
    secondary = ForestGreen,       // Verde orgánico para elementos secundarios
    onSecondary = Color.White,
    tertiary = CyberBlue,          // Cian para IA y clima
    onTertiary = Color.Black,
    background = TerminalBlack,    // Fondo negro profundo (OLED friendly)
    surface = TerminalGray,        // Superficies de tarjetas en gris oscuro hacker
    onBackground = Color.White,
    onSurface = Color.White,
    error = HackerRed,
    onError = Color.White
)

// Aunque prefieras el modo oscuro, definimos un Light por estándares, 
// usando tonos tierra y verdes claros.
private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    secondary = GreenDark,
    background = Color(0xFFF1F8E9), // Verde muy claro casi blanco
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun BitacoraPlantasTheme(
    darkTheme: Boolean = true, // Forzamos modo oscuro por estética fsociety
    dynamicColor: Boolean = false, // Deshabilitamos dinámico para mantener la identidad visual
    content: @Composable () -> Unit
) {
    // Siempre usamos DarkColorScheme para mantener la estética Hacker
    val colorScheme = DarkColorScheme 

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Colores personalizados para los campos de texto (TextFields)
 * para que brillen con el verde neón hacker.
 */
@Composable
fun outlinedFieldColors(): TextFieldColors {
    val colors = MaterialTheme.colorScheme
    
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = colors.onBackground,
        unfocusedTextColor = colors.onBackground.copy(alpha = 0.7f),
        focusedContainerColor = colors.surface,
        unfocusedContainerColor = colors.surface,
        cursorColor = colors.primary,
        focusedBorderColor = colors.primary, // Borde verde neón al escribir
        unfocusedBorderColor = colors.primary.copy(alpha = 0.4f),
        focusedLabelColor = colors.primary,
        unfocusedLabelColor = colors.onBackground.copy(alpha = 0.7f)
    )
}
