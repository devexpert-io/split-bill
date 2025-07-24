package io.devexpert.splitbill

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.devexpert.splitbill.data.MLKitTicketDataSource
import io.devexpert.splitbill.data.MockTicketDataSource
import io.devexpert.splitbill.data.TicketRepository
import io.devexpert.splitbill.data.ScanCounterRepository
import io.devexpert.splitbill.data.DataStoreScanCounterDataSource
import io.devexpert.splitbill.ui.theme.SplitBillTheme

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
                        HomeScreen(
                            ticketRepository = ticketRepository,
                            scanCounterRepository = scanCounterRepository,
                            onTicketProcessed = {
                                navController.navigate("receipt")
                            }
                        )
                    }

                    composable("receipt") {
                        ReceiptScreen(
                            ticketRepository = ticketRepository,
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