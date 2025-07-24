package io.devexpert.splitbill

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.devexpert.splitbill.data.MLKitTicketDataSource
import io.devexpert.splitbill.data.MockTicketDataSource
import io.devexpert.splitbill.data.TicketRepository
import io.devexpert.splitbill.data.ScanCounterRepository
import io.devexpert.splitbill.data.DataStoreScanCounterDataSource
import io.devexpert.splitbill.domain.usecases.DecrementScanCounterUseCase
import io.devexpert.splitbill.domain.usecases.GetScansRemainingUseCase
import io.devexpert.splitbill.domain.usecases.GetTicketDataUseCase
import io.devexpert.splitbill.domain.usecases.InitializeScanCounterUseCase
import io.devexpert.splitbill.domain.usecases.ProcessTicketUseCase
import io.devexpert.splitbill.ui.screens.home.HomeScreen
import io.devexpert.splitbill.ui.theme.SplitBillTheme
import io.devexpert.splitbill.ui.screens.home.HomeViewModel
import io.devexpert.splitbill.ui.screens.receipt.ReceiptScreen
import io.devexpert.splitbill.ui.screens.receipt.ReceiptViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val ticketDataSource = if (BuildConfig.DEBUG) {
            MockTicketDataSource()
        } else {
            MLKitTicketDataSource()
        }
        val ticketRepository = TicketRepository(ticketDataSource)
        
        val scanCounterDataSource = DataStoreScanCounterDataSource(this)
        val scanCounterRepository = ScanCounterRepository(scanCounterDataSource)

        setContent {
            SplitBillTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        val homeViewModel: HomeViewModel = viewModel {
                            HomeViewModel(
                                processTicketUseCase = ProcessTicketUseCase(ticketRepository),
                                initializeScanCounterUseCase = InitializeScanCounterUseCase(scanCounterRepository),
                                getScansRemainingUseCase = GetScansRemainingUseCase(scanCounterRepository),
                                decrementScanCounterUseCase = DecrementScanCounterUseCase(scanCounterRepository)
                            )
                        }
                        HomeScreen(
                            viewModel = homeViewModel,
                            onTicketProcessed = {
                                navController.navigate("receipt")
                            }
                        )
                    }

                    composable("receipt") {
                        val receiptViewModel: ReceiptViewModel = viewModel {
                            ReceiptViewModel(GetTicketDataUseCase(ticketRepository))
                        }
                        ReceiptScreen(
                            viewModel = receiptViewModel,
                            onBackPressed = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}