package com.tubitacora.plantas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tubitacora.plantas.R
import com.tubitacora.plantas.data.remote.RetrofitOpenRouterClient
import com.tubitacora.plantas.data.remote.dto.ChatRequest
import com.tubitacora.plantas.data.remote.dto.Message
import com.tubitacora.plantas.ui.IA.ChatMessage
import com.tubitacora.plantas.data.local.entity.PlantEntity
import com.tubitacora.plantas.data.local.entity.PlantLogEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class AiViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

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
                val request = ChatRequest(
                    model = "openrouter/aurora-alpha",
                    messages = _messages.value.takeLast(10).map { 
                        Message(role = if(it.isAi) "assistant" else "user", content = it.text) 
                    },
                    max_tokens = 2000,
                    temperature = 0.7
                )

                val response = RetrofitOpenRouterClient.service.chatCompletion(
                    authorization = "Bearer ${context.getString(R.string.openrouter_api_key)}",
                    request = request
                )

                val aiText = response.choices.firstOrNull()?.message?.content?.toString()
                    ?: "IA: No pude procesar la respuesta."

                _messages.value = _messages.value + ChatMessage(
                    text = aiText,
                    isAi = true,
                    content = aiText
                )

            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(
                    text = "Error: ${e.localizedMessage}",
                    isAi = true,
                    content = ""
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendPlantRecommendation(plant: PlantEntity, plantLogs: List<PlantLogEntity>) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val plantingDate = dateFormat.format(java.util.Date(plant.plantingDate))
        
        var prompt = "Eres un experto en botánica y agronomía. Tu misión es darme recomendaciones de cuidado para mi planta. Aquí tienes la información:\n\n"
        prompt += "- **Planta**: ${plant.name} (${plant.type})\n"
        prompt += "- **Fecha de siembra**: $plantingDate\n"
        if (!plant.notes.isNullOrBlank()) {
            prompt += "- **Notas generales**: ${plant.notes}\n"
        }
        
        if (plantLogs.isNotEmpty()) {
            prompt += "\n**Historial de cuidados recientes (bitácora):**\n"
            val recentLogs = plantLogs.takeLast(5) // Tomar los 5 registros más recientes
            recentLogs.forEach { log ->
                val logDate = dateFormat.format(java.util.Date(log.date))
                prompt += "- **${logDate}**: "
                
                val details = mutableListOf<String>()
                if (log.watered) details.add("Riego registrado")
                if (log.heightCm > 0f) details.add("Crecimiento de ${log.heightCm} cm")
                if (!log.fertilizerType.isNullOrBlank()) {
                    var fertilizerDetail = "Abono: ${log.fertilizerType}"
                    if (!log.fertilizerDose.isNullOrBlank()) {
                        fertilizerDetail += " (Dosis: ${log.fertilizerDose})"
                    }
                    details.add(fertilizerDetail)
                }
                if (!log.note.isNullOrBlank()) details.add("Nota: '${log.note}'")

                prompt += details.joinToString(", ") + ".\n"
            }
        }

        prompt += "\nBasado en toda esta información, dame recomendaciones detalladas de cuidado, riego y abono en español, en un formato claro y fácil de seguir."
        
        sendPrompt(prompt)
    }
}
