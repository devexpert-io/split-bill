package io.devexpert.splitbill

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TicketItem(
    val name: String,
    @SerialName("count") val quantity: Int,
    @SerialName("price_per_unit") val price: Double
)

@Serializable
data class TicketData(
    val items: List<TicketItem>,
    val total: Double
)

class TicketProcessor(private val useMockData: Boolean = false) {

    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        private val MOCK_JSON = """
        {
           "items":[
              {
                 "count":7,
                 "name":"PAN",
                 "price_per_unit":1.90
              },
              {
                 "count":3,
                 "name":"COCA COLA ZERO",
                 "price_per_unit":3.75
              },
              {
                 "count":3,
                 "name":"CAÑA",
                 "price_per_unit":4.20
              },
              {
                 "count":4,
                 "name":"AGUA SOLAN 1/2",
                 "price_per_unit":2.70
              },
              {
                 "count":1,
                 "name":"CROQUETAS DE LACON Y HUEV",
                 "price_per_unit":12.00
              },
              {
                 "count":1,
                 "name":"BUÑUELOS DE BACALAO",
                 "price_per_unit":12.50
              },
              {
                 "count":1,
                 "name":"ALCACHOFAS A LA PLANCHA",
                 "price_per_unit":16.00
              },
              {
                 "count":1,
                 "name":"PATATA RELLENA DE RABO",
                 "price_per_unit":16.00
              },
              {
                 "count":1,
                 "name":"JARRETE DE CERDO ASADO",
                 "price_per_unit":20.00
              },
              {
                 "count":2,
                 "name":"ARROZ NEGRO",
                 "price_per_unit":21.00
              },
              {
                 "count":1,
                 "name":"VERDURAS GUISADAS",
                 "price_per_unit":16.00
              },
              {
                 "count":1,
                 "name":"PIMIENTOS RELLENOS",
                 "price_per_unit":14.00
              },
              {
                 "count":1,
                 "name":"ARROZ SECO CON VERDURAS Y",
                 "price_per_unit":18.00
              },
              {
                 "count":1,
                 "name":"MERLUZA MEUNIER",
                 "price_per_unit":22.00
              },
              {
                 "count":1,
                 "name":"TARTA DE LIMON DE LA MARU",
                 "price_per_unit":7.00
              },
              {
                 "count":1,
                 "name":"TARTA DE QUESO DE CANADIO",
                 "price_per_unit":7.00
              },
              {
                 "count":1,
                 "name":"EL FLAN DE ALEX",
                 "price_per_unit":7.00
              },
              {
                 "count":2,
                 "name":"INFUSION",
                 "price_per_unit":3.50
              },
              {
                 "count":1,
                 "name":"CAFE CON LECHE",
                 "price_per_unit":2.75
              },
              {
                 "count":1,
                 "name":"CAFE SOLO",
                 "price_per_unit":2.50
              },
              {
                 "count":1,
                 "name":"CAFE CORTADO",
                 "price_per_unit":2.50
              }
           ],
           "total":272.20
        }
        """.trimIndent()
    }
    
    suspend fun processTicketImage(bitmap: Bitmap): Result<TicketData> = withContext(Dispatchers.IO) {
        try {
            if (useMockData) {
                Log.d("TicketProcessor", "Usando datos mock...")
                // Simular un poco de delay para que parezca real
                delay(1500)
                
                // Parsear los datos mock
                val ticketData = json.decodeFromString<TicketData>(MOCK_JSON)
                Result.success(ticketData)
            } else {
                Log.d("TicketProcessor", "Iniciando procesamiento de imagen...")

                // Definir el schema para la respuesta JSON (sin requiredProperties)
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
                val responseText = response.text ?: throw Exception("No se recibió respuesta de la IA")
                Log.d("TicketProcessor", "Respuesta de IA: $responseText")

                // Parsear el JSON usando kotlinx.serialization
                val ticketData = json.decodeFromString<TicketData>(responseText)
                Result.success(ticketData)
            }
        } catch (e: Exception) {
            Log.e("TicketProcessor", "Error procesando ticket: ${e.message}", e)
            Result.failure(e)
        }
    }
} 