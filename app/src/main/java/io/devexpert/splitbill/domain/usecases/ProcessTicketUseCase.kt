package io.devexpert.splitbill.domain.usecases

import io.devexpert.splitbill.data.TicketData
import io.devexpert.splitbill.data.TicketRepository

class ProcessTicketUseCase(private val ticketRepository: TicketRepository) {
    
    suspend operator fun invoke(imageBytes: ByteArray): TicketData {
        return ticketRepository.processTicket(imageBytes)
    }
}