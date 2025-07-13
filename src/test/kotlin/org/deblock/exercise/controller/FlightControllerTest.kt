package org.deblock.exercise.controller

import org.deblock.exercise.dto.FlightResult
import org.deblock.exercise.service.FlightService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.LocalDateTime

@WebMvcTest(FlightController::class)
class FlightControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var flightService: FlightService

    @Test
    fun flightSearch_success() {
        val mockResults = listOf(
            FlightResult(
                airline = "Airline1",
                supplier = "Provider1",
                fare = BigDecimal("100.00"),
                departureAirportCode = "FRA",
                destinationAirportCode = "LHR",
                departureDate = LocalDateTime.of(2023, 10, 1, 10, 0),
                arrivalDate = LocalDateTime.of(2023, 10, 1, 12, 0)
            ),
            FlightResult(
                airline = "Airline2",
                supplier = "Provider2",
                fare = BigDecimal("150.00"),
                departureAirportCode = "FRA",
                destinationAirportCode = "LHR",
                departureDate = LocalDateTime.of(2023, 10, 1, 11, 0),
                arrivalDate = LocalDateTime.of(2023, 10, 1, 13, 30)
            )
        )

        whenever(flightService.search(any())).thenReturn(mockResults)

        mockMvc.perform(
            get("/api/flight/search")
                .param("origin", "FRA")
                .param("destination", "LHR")
                .param("departureDate", "2025-10-01")
                .param("returnDate", "2025-10-02")
                .param("numberOfPassengers", "2")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.flights").isArray)
            .andExpect(jsonPath("$.flights.length()").value(2))
            .andExpect(jsonPath("$.flights[0].airline").value("Airline1"))
            .andExpect(jsonPath("$.flights[0].supplier").value("Provider1"))
            .andExpect(jsonPath("$.flights[0].fare").value(100.00))
            .andExpect(jsonPath("$.flights[0].departureAirportCode").value("FRA"))
            .andExpect(jsonPath("$.flights[0].destinationAirportCode").value("LHR"))
            .andExpect(jsonPath("$.flights[1].airline").value("Airline2"))
            .andExpect(jsonPath("$.flights[1].fare").value(150.00))
    }

    @Test
    fun flightSearch_missingRequiredParameter_returnsBadRequest() {
        mockMvc.perform(
            get("/api/flight/search")
                .param("origin", "FRA")
                .param("destination", "LHR")
                .param("departureDate", "2025-10-01")
                .param("numberOfPassengers", "1")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun flightSearch_emptyResults_returnsEmptyList() {
        whenever(flightService.search(any())).thenReturn(emptyList())

        mockMvc.perform(
            get("/api/flight/search")
                .param("origin", "FRA")
                .param("destination", "LHR")
                .param("departureDate", "2025-10-01")
                .param("returnDate", "2025-10-02")
                .param("numberOfPassengers", "1")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.flights").isArray)
            .andExpect(jsonPath("$.flights.length()").value(0))
    }
}
