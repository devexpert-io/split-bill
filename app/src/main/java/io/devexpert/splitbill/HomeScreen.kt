package io.devexpert.splitbill

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.devexpert.splitbill.ui.state.rememberCameraState
import io.devexpert.splitbill.ui.viewmodel.HomeViewModel
import io.devexpert.splitbill.ui.viewmodel.HomeUiState

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onTicketProcessed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraState = rememberCameraState(onImageCaptured = viewModel::processTicket)

    LaunchedEffect(uiState.ticketProcessed) {
        if (uiState.ticketProcessed) {
            onTicketProcessed()
            viewModel.resetTicketProcessed()
        }
    }

    HomeScreenContent(
        modifier = modifier,
        uiState = uiState,
        onScanClicked = cameraState::launchCamera
    )
}

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onScanClicked: () -> Unit
) {
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
                Text(
                    text = if (uiState.scansLeft > 0)
                        stringResource(R.string.scans_remaining, uiState.scansLeft)
                    else
                        stringResource(R.string.no_scans_remaining),
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                Button(
                    onClick = onScanClicked,
                    enabled = uiState.scansLeft > 0 && !uiState.isProcessing,
                    modifier = Modifier.size(width = 320.dp, height = 64.dp),
                    shape = ButtonDefaults.shape
                ) {
                    Text(
                        text = if (uiState.isProcessing)
                            stringResource(R.string.processing)
                        else
                            stringResource(R.string.scan_ticket),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                when {
                    uiState.isProcessing -> {
                        Text(
                            text = stringResource(R.string.photo_captured_processing),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                    uiState.errorMessage != null -> {
                        Text(
                            text = uiState.errorMessage,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
 