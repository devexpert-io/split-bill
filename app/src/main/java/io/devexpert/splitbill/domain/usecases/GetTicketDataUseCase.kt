package io.devexpert.splitbill.domain.usecases

import io.devexpert.splitbill.data.TicketData
import io.devexpert.splitbill.data.TicketRepository

class GetTicketDataUseCase(private val ticketRepository: TicketRepository) {
    
    operator fun invoke(): TicketData? {
        return ticketRepository.getTicketData()
    }
}