package io.devexpert.splitbill.ui.state

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

class CameraState(
    private val context: Context,
    private val cameraLauncher: ManagedActivityResultLauncher<Uri, Boolean>,
    private val onUriChanged: (Uri) -> Unit
) {
    fun launchCamera() {
        val photoFile = File.createTempFile("ticket_", ".jpg", context.cacheDir)
        val uri = FileProvider.getUriForFile(
            context,
            "io.devexpert.splitbill.fileprovider",
            photoFile
        )
        onUriChanged(uri)
        cameraLauncher.launch(uri)
    }
}

@Composable
fun rememberCameraState(
    onImageCaptured: (Bitmap) -> Unit
): CameraState {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && photoUri != null) {
            val inputStream = context.contentResolver.openInputStream(photoUri!!)
            val bitmap = inputStream?.use { BitmapFactory.decodeStream(it) }
            if (bitmap != null) {
                onImageCaptured(bitmap)
            }
        }
    }

    return remember(context, cameraLauncher) {
        CameraState(context, cameraLauncher) { uri ->
            photoUri = uri
        }
    }
}