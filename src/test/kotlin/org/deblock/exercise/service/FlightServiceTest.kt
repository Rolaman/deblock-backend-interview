package org.deblock.exercise.service

import org.assertj.core.api.Assertions.assertThat
import org.deblock.exercise.dto.FlightResult
import org.deblock.exercise.dto.FlightSearchRequest
import org.deblock.exercise.provider.FlightProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class FlightServiceTest {

    private lateinit var provider1: FlightProvider
    private lateinit var provider2: FlightProvider
    private lateinit var flightService: FlightService
    private lateinit var searchRequest: FlightSearchRequest

    @BeforeEach
    fun setUp() {
        provider1 = mock()
        provider2 = mock()
        
        searchRequest = FlightSearchRequest(
            origin = "FRA",
            destination = "LHR",
            departureDate = LocalDate.now().plusDays(1),
            returnDate = LocalDate.now().plusDays(2),
            numberOfPassengers = 2,
        )
    }

    @Test
    fun search_success_multipleProviders() {
        val provider1Results = listOf(
            FlightResult(
                airline = "Lufthansa",
                supplier = "Provider1",
                fare = BigDecimal("150.00"),
                departureAirportCode = "FRA",
                destinationAirportCode = "LHR",
                departureDate = LocalDateTime.of(2023, 10, 1, 10, 0),
                arrivalDate = LocalDateTime.of(2023, 10, 1, 12, 0),
            )
        )
        
        val provider2Results = listOf(
            FlightResult(
                airline = "Air France",
                supplier = "Provider2",
                fare = BigDecimal("120.00"),
                departureAirportCode = "FRA",
                destinationAirportCode = "LHR",
                departureDate = LocalDateTime.of(2023, 10, 1, 9, 0),
                arrivalDate = LocalDateTime.of(2023, 10, 1, 11, 0),
            )
        )

        whenever(provider1.search(searchRequest)).thenReturn(provider1Results)
        whenever(provider2.search(searchRequest)).thenReturn(provider2Results)
        
        flightService = FlightService(listOf(provider1, provider2))

        val results = flightService.search(searchRequest)

        assertThat(results).hasSize(2)
        assertThat(results[0].fare).isEqualTo(BigDecimal("120.00"))
        assertThat(results[0].supplier).isEqualTo("Provider2")
        assertThat(results[1].fare).isEqualTo(BigDecimal("150.00"))
        assertThat(results[1].supplier).isEqualTo("Provider1")
    }

    @Test
    fun search_success_emptyResults() {
        whenever(provider1.search(searchRequest)).thenReturn(emptyList())
        whenever(provider2.search(searchRequest)).thenReturn(emptyList())

        flightService = FlightService(listOf(provider1, provider2))

        val results = flightService.search(searchRequest)

        assertThat(results).isEmpty()
    }

    @Test
    fun search_success_oneProviderFails() {
        val provider2Results = listOf(
            FlightResult(
                airline = "KLM",
                supplier = "Provider2",
                fare = BigDecimal("180.00"),
                departureAirportCode = "FRA",
                destinationAirportCode = "LHR",
                departureDate = LocalDateTime.of(2023, 10, 1, 12, 0),
                arrivalDate = LocalDateTime.of(2023, 10, 1, 14, 0),
            )
        )

        whenever(provider1.search(searchRequest)).thenThrow(RuntimeException("Provider1 failed"))
        whenever(provider2.search(searchRequest)).thenReturn(provider2Results)
        
        flightService = FlightService(listOf(provider1, provider2))

        val results = flightService.search(searchRequest)

        assertThat(results).hasSize(1)
        assertThat(results[0].supplier).isEqualTo("Provider2")
        assertThat(results[0].fare).isEqualTo(BigDecimal("180.00"))
    }

    @Test
    fun search_success_noProviders() {
        flightService = FlightService(emptyList())
        val results = flightService.search(searchRequest)

        assertThat(results).isEmpty()
    }
}
