package io.devexpert.splitbill.di

import android.content.Context
import io.devexpert.splitbill.BuildConfig
import io.devexpert.splitbill.data.DataStoreScanCounterDataSource
import io.devexpert.splitbill.data.MLKitTicketDataSource
import io.devexpert.splitbill.data.MockTicketDataSource
import io.devexpert.splitbill.data.ScanCounterRepository
import io.devexpert.splitbill.data.TicketRepository
import io.devexpert.splitbill.domain.usecases.DecrementScanCounterUseCase
import io.devexpert.splitbill.domain.usecases.GetScansRemainingUseCase
import io.devexpert.splitbill.domain.usecases.GetTicketDataUseCase
import io.devexpert.splitbill.domain.usecases.InitializeScanCounterUseCase
import io.devexpert.splitbill.domain.usecases.ProcessTicketUseCase
import io.devexpert.splitbill.ui.screens.home.HomeViewModel
import io.devexpert.splitbill.ui.screens.receipt.ReceiptViewModel

object AppModule {
    
    private var ticketRepository: TicketRepository? = null
    private var scanCounterRepository: ScanCounterRepository? = null
    
    fun provideTicketRepository(): TicketRepository {
        if (ticketRepository == null) {
            val ticketDataSource = if (BuildConfig.DEBUG) {
                MockTicketDataSource()
            } else {
                MLKitTicketDataSource()
            }
            ticketRepository = TicketRepository(ticketDataSource)
        }
        return ticketRepository!!
    }
    
    fun provideScanCounterRepository(context: Context): ScanCounterRepository {
        if (scanCounterRepository == null) {
            val scanCounterDataSource = DataStoreScanCounterDataSource(context)
            scanCounterRepository = ScanCounterRepository(scanCounterDataSource)
        }
        return scanCounterRepository!!
    }
    
    fun provideProcessTicketUseCase(): ProcessTicketUseCase {
        return ProcessTicketUseCase(provideTicketRepository())
    }
    
    fun provideInitializeScanCounterUseCase(context: Context): InitializeScanCounterUseCase {
        return InitializeScanCounterUseCase(provideScanCounterRepository(context))
    }
    
    fun provideGetScansRemainingUseCase(context: Context): GetScansRemainingUseCase {
        return GetScansRemainingUseCase(provideScanCounterRepository(context))
    }
    
    fun provideDecrementScanCounterUseCase(context: Context): DecrementScanCounterUseCase {
        return DecrementScanCounterUseCase(provideScanCounterRepository(context))
    }
    
    fun provideGetTicketDataUseCase(): GetTicketDataUseCase {
        return GetTicketDataUseCase(provideTicketRepository())
    }
    
    // ViewModel Factory Functions
    fun createHomeViewModel(context: Context): HomeViewModel {
        return HomeViewModel(
            processTicketUseCase = provideProcessTicketUseCase(),
            initializeScanCounterUseCase = provideInitializeScanCounterUseCase(context),
            getScansRemainingUseCase = provideGetScansRemainingUseCase(context),
            decrementScanCounterUseCase = provideDecrementScanCounterUseCase(context)
        )
    }
    
    fun createReceiptViewModel(): ReceiptViewModel {
        return ReceiptViewModel(provideGetTicketDataUseCase())
    }
}