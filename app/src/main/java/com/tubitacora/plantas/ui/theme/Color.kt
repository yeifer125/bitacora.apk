package com.tubitacora.plantas.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 🌿 PALETA PROFESIONAL "CYBER-FOREST"
 * Una mezcla entre la naturaleza viva y la estética terminal hacker.
 */

// --- COLORES PRIMARIOS (Hacker Matrix) ---
val GreenAccent = Color(0xFF00FF41)    // Verde Neón Terminal (Foco principal)
val GreenDark = Color(0xFF003B00)      // Verde muy oscuro para fondos de botones

// --- COLORES DE NATURALEZA (Organic) ---
val ForestGreen = Color(0xFF2E7D32)    // Verde bosque (Para elementos de plantas)
val EarthBrown = Color(0xFF3E2723)     // Marrón tierra (Para contrastes orgánicos)

// --- COLORES DE SISTEMA (Retro Terminal) ---
val TerminalBlack = Color(0xFF0A0A0A)  // Negro casi puro (Fondo principal)
val TerminalGray = Color(0xFF1A1A1A)   // Gris oscuro (Para Cards y superficies)
val PhosphorAmber = Color(0xFFFFB300)  // Ámbar clásico (Para advertencias o stats secundarios)

// --- COLORES DE ESTADO ---
val HackerRed = Color(0xFFFF3D00)      // Rojo vibrante (Errores/Peligros)
val CyberBlue = Color(0xFF00E5FF)      // Azul cian (Para IA y datos meteorológicos)

// --- COMPATIBILIDAD MATERIAL 3 (Opcional pero recomendado) ---
val Purple80 = GreenAccent
val PurpleGrey80 = ForestGreen
val Pink80 = CyberBlue

val Purple40 = GreenDark
val PurpleGrey40 = EarthBrown
val Pink40 = HackerRed

// Mantengo MyGreen por compatibilidad con tu código actual
val MyGreen = ForestGreen
val MyOrange = PhosphorAmber
val MyRed = HackerRed
val MyBlue = CyberBlue
