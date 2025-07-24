package io.devexpert.splitbill.ui.screens.receipt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.devexpert.splitbill.R
import io.devexpert.splitbill.data.TicketData
import io.devexpert.splitbill.data.TicketItem
import io.devexpert.splitbill.di.AppModule
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    viewModel: ReceiptViewModel = viewModel { AppModule.createReceiptViewModel() },
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    ReceiptScreen(
        uiState = uiState,
        onBackPressed = onBackPressed,
        onQuantityChange = viewModel::onQuantityChange,
        onMarkAsPaid = viewModel::onMarkAsPaid
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    uiState: ReceiptUiState,
    onBackPressed: () -> Unit,
    onQuantityChange: (TicketItem, Int) -> Unit,
    onMarkAsPaid: () -> Unit
) {
    if (uiState.ticketData == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.no_ticket_data),
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = onBackPressed) {
                Text(stringResource(R.string.back))
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.receipt)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.availableItems) { (item, availableQty) ->
                    val selectedQty = uiState.selectedQuantities[item] ?: 0

                    SelectableTicketItemCard(
                        item = item,
                        availableQuantity = availableQty,
                        selectedQuantity = selectedQty,
                        onQuantityChange = { newQty ->
                            onQuantityChange(item, newQty)
                        }
                    )
                }

                items(uiState.paidItems) { (item, paidQty) ->
                    PaidTicketItemCard(
                        item = item,
                        paidQuantity = paidQty
                    )
                }
            }

            if (uiState.selectedTotal > 0) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.selected_total),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "€${
                                    String.format(
                                        Locale.getDefault(),
                                        "%.2f",
                                        uiState.selectedTotal
                                    )
                                }",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onMarkAsPaid,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.mark_as_paid),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableTicketItemCard(
    item: TicketItem,
    availableQuantity: Int,
    selectedQuantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${availableQuantity}x",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "€${String.format(Locale.getDefault(), "%.2f", item.price)} ${
                        stringResource(
                            R.string.each
                        )
                    }",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (availableQuantity == 1) {
                Checkbox(
                    checked = selectedQuantity > 0,
                    onCheckedChange = { checked ->
                        onQuantityChange(if (checked) 1 else 0)
                    }
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (selectedQuantity > 0) {
                                onQuantityChange(selectedQuantity - 1)
                            }
                        },
                        enabled = selectedQuantity > 0
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Remove,
                            contentDescription = stringResource(R.string.reduce_quantity)
                        )
                    }

                    Text(
                        text = selectedQuantity.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    IconButton(
                        onClick = {
                            if (selectedQuantity < availableQuantity) {
                                onQuantityChange(selectedQuantity + 1)
                            }
                        },
                        enabled = selectedQuantity < availableQuantity
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.increase_quantity)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaidTicketItemCard(
    item: TicketItem,
    paidQuantity: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${paidQuantity}x",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.LineThrough
                )
                Text(
                    text = "€${String.format(Locale.getDefault(), "%.2f", item.price)} ${
                        stringResource(
                            R.string.each
                        )
                    }",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.LineThrough
                )
            }

            Text(
                text = "€${String.format(Locale.getDefault(), "%.2f", item.price * paidQuantity)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReceiptScreenContentPreview() {
    val sampleItems = listOf(
        TicketItem(name = "Pizza Margherita", quantity = 2, price = 12.50),
        TicketItem(name = "Pasta Carbonara", quantity = 1, price = 14.00),
        TicketItem(name = "Tiramisu", quantity = 2, price = 6.50)
    )

    val sampleTicketData = TicketData(
        items = sampleItems,
        total = 51.50
    )

    ReceiptScreen(
        uiState = ReceiptUiState(
            ticketData = sampleTicketData,
            availableItems = listOf(
                sampleItems[0] to 2,
                sampleItems[1] to 1,
                sampleItems[2] to 1
            ),
            paidItems = listOf(
                sampleItems[2] to 1
            ),
            selectedQuantities = mapOf(
                sampleItems[0] to 1,
                sampleItems[1] to 1
            ),
            selectedTotal = 26.50
        ),
        onBackPressed = {},
        onQuantityChange = { _, _ -> },
        onMarkAsPaid = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ReceiptScreenContentEmptyPreview() {
    ReceiptScreen(
        uiState = ReceiptUiState(ticketData = null),
        onBackPressed = {},
        onQuantityChange = { _, _ -> },
        onMarkAsPaid = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ReceiptScreenContentAllPaidPreview() {
    val sampleItems = listOf(
        TicketItem(name = "Hamburger", quantity = 1, price = 8.50),
        TicketItem(name = "French Fries", quantity = 1, price = 4.00)
    )

    val sampleTicketData = TicketData(
        items = sampleItems,
        total = 12.50
    )

    ReceiptScreen(
        uiState = ReceiptUiState(
            ticketData = sampleTicketData,
            availableItems = emptyList(),
            paidItems = listOf(
                sampleItems[0] to 1,
                sampleItems[1] to 1
            ),
            selectedQuantities = emptyMap(),
            selectedTotal = 0.0
        ),
        onBackPressed = {},
        onQuantityChange = { _, _ -> },
        onMarkAsPaid = {}
    )
}
 