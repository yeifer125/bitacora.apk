package com.tubitacora.plantas.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.tubitacora.plantas.data.remote.RetrofitGoogleClient
import com.tubitacora.plantas.ui.IA.ChatMessage
import com.tubitacora.plantas.data.local.entity.PlantEntity
import com.tubitacora.plantas.data.local.entity.PlantLogEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class AiViewModel(application: Application) : AndroidViewModel(application) {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val generativeModel = GenerativeModel(
        modelName = "gemini-flash-latest",
        apiKey = RetrofitGoogleClient.GEMINI_API_KEY
    )

    fun sendPrompt(userInput: String) {
        if (userInput.isBlank()) return

        _messages.value = _messages.value + ChatMessage(
            text = userInput,
            isAi = false,
            content = userInput
        )

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = generativeModel.generateContent(userInput)
                val aiText = response.text ?: "IA: No pude procesar la respuesta."

                _messages.value = _messages.value + ChatMessage(
                    text = aiText,
                    isAi = true,
                    content = aiText
                )

            } catch (e: Exception) {
                Log.e("GeminiError", "Error con SDK: ${e.message}", e)
                _messages.value = _messages.value + ChatMessage(
                    text = "Error: Verifica tu conexión o API KEY.",
                    isAi = true,
                    content = ""
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 🌿 PROMPT MAESTRO PARA ASESORÍA AGRONÓMICA
     * Envía toda la información disponible de la planta y su bitácora a la IA.
     */
    fun sendPlantRecommendation(plant: PlantEntity, plantLogs: List<PlantLogEntity>) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val plantingDate = dateFormat.format(java.util.Date(plant.plantingDate))
        
        var prompt = "🤖 ACTÚA COMO UN AGRÓNOMO EXPERTO. Mi misión es cuidar esta planta y necesito tu asesoría técnica.\n\n"
        
        prompt += "📝 **DATOS DE LA PLANTA:**\n"
        prompt += "- **Nombre:** ${plant.name}\n"
        prompt += "- **Especie/Tipo:** ${plant.type}\n"
        prompt += "- **Fecha de siembra:** $plantingDate\n"
        prompt += "- **Frecuencia de riego recomendada:** cada ${plant.wateringFrequencyDays} días\n"
        if (!plant.notes.isNullOrBlank()) {
            prompt += "- **Notas generales:** ${plant.notes}\n"
        }

        if (plantLogs.isNotEmpty()) {
            prompt += "\n📊 **HISTORIAL DE BITÁCORA (Últimos registros):**\n"
            // Tomamos los últimos 10 registros para dar contexto histórico
            val sortedLogs = plantLogs.sortedByDescending { it.date }.take(10)
            
            sortedLogs.forEach { log ->
                val logDate = dateFormat.format(java.util.Date(log.date))
                prompt += "📍 $logDate:\n"
                prompt += "   - Estado: ${log.status}\n"
                if (log.watered) prompt += "   - ✅ Se realizó riego.\n"
                if (log.heightCm > 0f) prompt += "   - Altura registrada: ${log.heightCm} cm\n"
                if (!log.fertilizerType.isNullOrBlank()) {
                    prompt += "   - 💊 Abono: ${log.fertilizerType} (Dosis: ${log.fertilizerDose ?: "No especificada"})\n"
                }
                if (!log.note.isNullOrBlank()) prompt += "   - 📝 Nota del día: '${log.note}'\n"
            }
        } else {
            prompt += "\n⚠️ No hay registros previos en la bitácora aún."
        }

        prompt += "\n\n💡 **TAREA:** Basado en esta información, dame un análisis técnico breve:\n"
        prompt += "1. ¿Cómo ves su progreso (crecimiento y salud)?\n"
        prompt += "2. Recomendaciones de riego o abono para los próximos días.\n"
        prompt += "3. Algún consejo experto específico para su especie: ${plant.type}.\n\n"
        prompt += "Responde en español de forma profesional y clara."
        
        sendPrompt(prompt)
    }
}
