package io.devexpert.splitbill.domain.usecases

import io.devexpert.splitbill.data.TestTicketDataSource
import io.devexpert.splitbill.data.TicketRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ProcessTicketUseCaseTest {

    private lateinit var testTicketDataSource: TestTicketDataSource
    private lateinit var ticketRepository: TicketRepository
    private lateinit var processTicketUseCase: ProcessTicketUseCase

    @Before
    fun setUp() {
        testTicketDataSource = TestTicketDataSource()
        ticketRepository = TicketRepository(testTicketDataSource)
        processTicketUseCase = ProcessTicketUseCase(ticketRepository)
    }

    @Test
    fun `invoke processes ticket with mock data successfully`() = runTest {
        // Given
        val imageBytes = byteArrayOf(1, 2, 3, 4, 5)

        // When
        val result = processTicketUseCase(imageBytes)

        // Then
        assertEquals(272.20, result.total, 0.01)
    }

}