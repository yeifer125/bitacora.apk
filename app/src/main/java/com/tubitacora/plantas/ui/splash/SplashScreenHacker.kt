package com.tubitacora.plantas.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tubitacora.plantas.ui.theme.GreenAccent
import kotlinx.coroutines.delay

@Composable
fun SplashScreenHacker(onTimeout: () -> Unit) {
    // Lógica para saltar a la Home después de 3 segundos
    LaunchedEffect(Unit) {
        delay(3000)
        onTimeout()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Arte ASCII Monumental
        Text(
            text = """
                ██████╗ █████╗ ██████╗ ██████╗  ██████╗ ███╗   ██╗ █████╗ ████████╗ ██████╗ 
                ██╔════╝██╔══██╗██╔══██╗██╔══██╗██╔═══██╗████╗  ██║██╔══██╗╚══██╔══╝██╔═══██╗
                ██║     ███████║██████╔╝██████╔╝██║   ██║██╔██╗ ██║███████║   ██║   ██║   ██║
                ██║     ██╔══██║██╔══██╗██╔══██╗██║   ██║██║╚██╗██║██╔══██║   ██║   ██║   ██║
                ╚██████╗██║  ██║██║  ██║██████╔╝╚██████╔╝██║ ╚████║██║  ██║   ██║   ╚██████╔╝
                 ╚═════╝╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝  ╚═════╝ ╚═╝  ╚═══╝╚═╝  ╚═╝   ╚═╝    ╚═════╝ 
            """.trimIndent(),
            color = GreenAccent,
            fontFamily = FontFamily.Monospace,
            fontSize = 5.sp,
            lineHeight = 7.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Log de Consola Hacker
        Column(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "> USER: cArbonAto\n" +
                       "> MODE: ACTIVIST_HACKER\n" +
                       "> STATUS: CONNECTED\n" +
                       "> ANONIMOUS\n" +
                       "> ACCESSING LUNAR_CORE...",
                color = GreenAccent,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        // Barra de Carga de Sistema
        LinearProgressIndicator(
            modifier = Modifier
                .width(200.dp)
                .height(2.dp),
            color = GreenAccent,
            trackColor = Color.DarkGray
        )
        
        Text(
            text = "DECRYPTING_NATURE...",
            color = GreenAccent.copy(alpha = 0.5f),
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
