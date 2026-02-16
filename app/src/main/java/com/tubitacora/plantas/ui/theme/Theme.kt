package com.tubitacora.plantas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// -------------------- Colores --------------------
private val DarkColorScheme = darkColorScheme(
    primary = MyBlue,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = yei2,
    surface = MyBlue,
    onPrimary = MyGold,
    onSecondary = MyGreen,
    onTertiary = MyGold,
    onBackground = MyGold,
    onSurface = yei
)

private val LightColorScheme = lightColorScheme(
    primary = MyGreen,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = MyOrange,
    surface = MyGold,
    onPrimary = yei,
    onSecondary = yei,
    onTertiary = yei,
    onBackground = MyOrange,
    onSurface = MyOrange
)

// -------------------- Theme Composable --------------------
@Composable
fun BitacoraPlantasTheme(
    darkTheme: Boolean = true,        // siempre oscuro
    dynamicColor: Boolean = true,    // deshabilitamos colores dinámicos
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// -------------------- Colores para OutlinedTextField --------------------
@Composable
fun outlinedFieldColors(): TextFieldColors {
    val colors = MaterialTheme.colorScheme
    val alphaUnfocused = 0.5f
    val alphaDisabled = 0.2f

    return OutlinedTextFieldDefaults.colors(
        // Texto
        focusedTextColor = colors.onBackground,
        unfocusedTextColor = colors.onBackground.copy(alpha = alphaUnfocused),
        disabledTextColor = colors.onBackground.copy(alpha = alphaDisabled),

        // Contenedor
        focusedContainerColor = colors.surface,
        unfocusedContainerColor = colors.surface,
        disabledContainerColor = colors.surface,

        // Cursor
        cursorColor = colors.primary,
        errorCursorColor = Color.Red,

        // Bordes
        focusedBorderColor = colors.primary,
        unfocusedBorderColor = colors.onBackground.copy(alpha = alphaUnfocused),
        disabledBorderColor = colors.onBackground.copy(alpha = alphaDisabled),
        errorBorderColor = Color.Red,

        // Labels
        focusedLabelColor = colors.primary,
        unfocusedLabelColor = colors.onBackground.copy(alpha = alphaUnfocused),
        disabledLabelColor = colors.onBackground.copy(alpha = alphaDisabled),
        errorLabelColor = Color.Red,

        // Placeholder
        focusedPlaceholderColor = colors.onBackground.copy(alpha = alphaUnfocused),
        unfocusedPlaceholderColor = colors.onBackground.copy(alpha = alphaUnfocused),
        disabledPlaceholderColor = colors.onBackground.copy(alpha = alphaDisabled),
        errorPlaceholderColor = Color.Red
    )
}

