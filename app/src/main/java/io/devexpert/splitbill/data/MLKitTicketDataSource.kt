package io.devexpert.splitbill.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import io.devexpert.splitbill.ui.ImageConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class MLKitTicketDataSource : TicketDataSource {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun processTicket(imageBytes: ByteArray): TicketData =
        withContext(Dispatchers.IO) {
            try {
                Log.d("MLKitTicketDataSource", "Procesando imagen de ${imageBytes.size} bytes...")

                val bitmap = ImageConverter.toBitmap(imageBytes)

                val jsonSchema = Schema.obj(
                    mapOf(
                        "items" to Schema.array(
                            Schema.obj(
                                mapOf(
                                    "name" to Schema.string(),
                                    "count" to Schema.integer(),
                                    "price_per_unit" to Schema.double()
                                )
                            )
                        ),
                        "total" to Schema.double()
                    )
                )

                val prompt = """
                Analiza esta imagen de un ticket de restaurante y extrae:
                1. Lista de items con nombre, cantidad y precio individual
                2. Total de la cuenta
                Si no puedes leer algún precio, ponlo como 0.0
            """.trimIndent()

                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }

                val model = Firebase.ai.generativeModel(
                    modelName = "gemini-2.5-flash-lite-preview-06-17",
                    generationConfig = generationConfig {
                        responseMimeType = "application/json"
                        responseSchema = jsonSchema
                    }
                )

                val response = model.generateContent(inputContent)
                val responseText =
                    response.text ?: throw Exception("No se recibió respuesta de la IA")
                Log.d("MLKitTicketDataSource", "Respuesta de IA: $responseText")

                json.decodeFromString<TicketData>(responseText)
            } catch (e: Exception) {
                Log.e("MLKitTicketDataSource", "Error procesando ticket: ${e.message}", e)
                throw e
            }
        }
}