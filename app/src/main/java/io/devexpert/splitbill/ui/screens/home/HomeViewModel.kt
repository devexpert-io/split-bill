package io.devexpert.splitbill.ui.screens.home

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.devexpert.splitbill.domain.usecases.ProcessTicketUseCase
import io.devexpert.splitbill.domain.usecases.InitializeScanCounterUseCase
import io.devexpert.splitbill.domain.usecases.GetScansRemainingUseCase
import io.devexpert.splitbill.domain.usecases.DecrementScanCounterUseCase
import io.devexpert.splitbill.ui.ImageConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val processTicketUseCase: ProcessTicketUseCase,
    private val initializeScanCounterUseCase: InitializeScanCounterUseCase,
    private val getScansRemainingUseCase: GetScansRemainingUseCase,
    private val decrementScanCounterUseCase: DecrementScanCounterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        initializeScanCounter()
        loadScansRemaining()
    }

    private fun initializeScanCounter() {
        viewModelScope.launch {
            initializeScanCounterUseCase()
        }
    }

    private fun loadScansRemaining() {
        viewModelScope.launch {
            getScansRemainingUseCase().collect { scans ->
                _uiState.value = _uiState.value.copy(scansLeft = scans)
            }
        }
    }

    fun processTicket(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)
            try {
                val imageBytes = ImageConverter.toResizedByteArray(bitmap)
                processTicketUseCase(imageBytes)
                decrementScanCounterUseCase()
                _uiState.value = _uiState.value.copy(isProcessing = false, ticketProcessed = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isProcessing = false, errorMessage = e.message)
            }
        }
    }
    
    fun resetTicketProcessed() {
        _uiState.value = _uiState.value.copy(ticketProcessed = false)
    }
}

data class HomeUiState(
    val scansLeft: Int = 0,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val ticketProcessed: Boolean = false
)
