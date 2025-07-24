package io.devexpert.splitbill

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
import io.devexpert.splitbill.data.TicketRepository
import io.devexpert.splitbill.data.ScanCounterRepository
import io.devexpert.splitbill.domain.usecases.ProcessTicketUseCase
import io.devexpert.splitbill.domain.usecases.InitializeScanCounterUseCase
import io.devexpert.splitbill.domain.usecases.GetScansRemainingUseCase
import io.devexpert.splitbill.domain.usecases.DecrementScanCounterUseCase
import io.devexpert.splitbill.ui.ImageConverter
import kotlinx.coroutines.launch
import java.io.File

// El Composable principal de la pantalla de inicio
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    ticketRepository: TicketRepository,
    scanCounterRepository: ScanCounterRepository,
    onTicketProcessed: () -> Unit
) {
    val context = LocalContext.current
    
    // Casos de uso
    val processTicketUseCase = remember { ProcessTicketUseCase(ticketRepository) }
    val initializeScanCounterUseCase = remember { InitializeScanCounterUseCase(scanCounterRepository) }
    val getScansRemainingUseCase = remember { GetScansRemainingUseCase(scanCounterRepository) }
    val decrementScanCounterUseCase = remember { DecrementScanCounterUseCase(scanCounterRepository) }
    
    val scansLeft by getScansRemainingUseCase().collectAsState(initial = 0)
    val isButtonEnabled = scansLeft > 0

    // Inicializar o resetear si es necesario al cargar la pantalla
    LaunchedEffect(Unit) {
        initializeScanCounterUseCase()
    }

    // Estado para mostrar el resultado del procesamiento
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Coroutine scope para operaciones asíncronas
    val coroutineScope = rememberCoroutineScope()

    var photoUri by remember { mutableStateOf<Uri?>(null) }


    // Launcher para capturar foto con la cámara (alta resolución)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && photoUri != null) {
            val inputStream = context.contentResolver.openInputStream(photoUri!!)
            val bitmap = inputStream?.use { BitmapFactory.decodeStream(it) }
            if (bitmap != null) {
                isProcessing = true
                errorMessage = null
                // Convertir Bitmap a ByteArray y procesar con IA
                coroutineScope.launch {
                    try {
                        val imageBytes = ImageConverter.toResizedByteArray(bitmap)
                        processTicketUseCase(imageBytes)
                        // Decrementar el contador solo si el procesamiento fue exitoso
                        decrementScanCounterUseCase()
                        isProcessing = false
                        // Llamar al callback para navegar a la siguiente pantalla
                        onTicketProcessed()
                    } catch (error: Exception) {
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