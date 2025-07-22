package io.devexpert.splitbill

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import java.io.File
import io.devexpert.splitbill.BuildConfig
import androidx.core.graphics.scale

// El Composable principal de la pantalla de inicio
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onTicketProcessed: (TicketData) -> Unit
) {
    // Variable local para los escaneos restantes (ahora desde DataStore)
    val context = LocalContext.current
    val scanCounter = remember { ScanCounter(context) }
    val scansLeft by scanCounter.scansRemaining.collectAsState(initial = 5)
    val isButtonEnabled = scansLeft > 0

    // Inicializar o resetear si es necesario al cargar la pantalla
    LaunchedEffect(Unit) {
        scanCounter.initializeOrResetIfNeeded()
    }

    // Estado para mostrar el resultado del procesamiento
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Coroutine scope para operaciones asíncronas
    val coroutineScope = rememberCoroutineScope()
    val ticketProcessor = remember { TicketProcessor(useMockData = BuildConfig.DEBUG) }

    var photoUri by remember { mutableStateOf<Uri?>(null) }

    fun resizeBitmapToMaxWidth(bitmap: Bitmap, maxWidth: Int): Bitmap {
        if (bitmap.width <= maxWidth) return bitmap
        val aspectRatio = bitmap.height.toFloat() / bitmap.width
        val newWidth = maxWidth
        val newHeight = (maxWidth * aspectRatio).toInt()
        return bitmap.scale(newWidth, newHeight)
    }

    // Launcher para capturar foto con la cámara (alta resolución)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && photoUri != null) {
            val inputStream = context.contentResolver.openInputStream(photoUri!!)
            val bitmap = inputStream?.use { BitmapFactory.decodeStream(it) }
            if (bitmap != null) {
                // Redimensionar antes de procesar
                val resizedBitmap = resizeBitmapToMaxWidth(bitmap, 1280)
                isProcessing = true
                errorMessage = null
                // Procesar la imagen con IA
                coroutineScope.launch {
                    ticketProcessor.processTicketImage(resizedBitmap)
                        .onSuccess { ticketData ->
                            // Decrementar el contador solo si el procesamiento fue exitoso
                            scanCounter.decrementScan()
                            isProcessing = false
                            // Llamar al callback para navegar a la siguiente pantalla
                            onTicketProcessed(ticketData)
                        }
                        .onFailure { error ->
                            errorMessage = context.getString(
                                R.string.error_processing_ticket,
                                error.message ?: ""
                            )
                            isProcessing = false
                        }
                }
            } else {
                errorMessage = context.getString(R.string.could_not_read_image)
            }
        }
    }

    Scaffold { padding ->
        Box(
            modifier = modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Contador de escaneos
                Text(
                    text = if (scansLeft > 0)
                        stringResource(R.string.scans_remaining, scansLeft)
                    else
                        stringResource(R.string.no_scans_remaining),
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                // Botón principal
                Button(
                    onClick = {
                        if (isButtonEnabled && !isProcessing) {
                            // Crear archivo temporal para la foto
                            val photoFile = File.createTempFile("ticket_", ".jpg", context.cacheDir)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "io.devexpert.splitbill.fileprovider",
                                photoFile
                            )
                            photoUri = uri
                            cameraLauncher.launch(uri)
                        }
                    },
                    enabled = isButtonEnabled && !isProcessing,
                    modifier = Modifier.size(width = 320.dp, height = 64.dp),
                    shape = ButtonDefaults.shape
                ) {
                    Text(
                        text = if (isProcessing)
                            stringResource(R.string.processing)
                        else
                            stringResource(R.string.scan_ticket),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Mostrar resultado del procesamiento
                when {
                    isProcessing -> {
                        Text(
                            text = stringResource(R.string.photo_captured_processing),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    errorMessage != null -> {
                        Text(
                            text = errorMessage!!,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
} 