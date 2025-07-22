package io.devexpert.splitbill.data

interface TicketDataSource {
    suspend fun processTicket(imageBytes: ByteArray): TicketData
}