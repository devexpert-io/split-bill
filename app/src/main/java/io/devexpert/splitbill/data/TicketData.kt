package io.devexpert.splitbill.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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