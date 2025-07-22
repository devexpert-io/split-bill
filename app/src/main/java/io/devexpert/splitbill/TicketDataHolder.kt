package io.devexpert.splitbill

/**
 * Singleton para compartir datos del ticket entre pantallas
 */
object TicketDataHolder {
    private var _ticketData: TicketData? = null

    fun setTicketData(data: TicketData) {
        _ticketData = data
    }

    fun getTicketData(): TicketData? = _ticketData

} 