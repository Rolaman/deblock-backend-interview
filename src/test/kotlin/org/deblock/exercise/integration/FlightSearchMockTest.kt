package org.deblock.exercise.integration

import org.assertj.core.api.Assertions.assertThat
import org.deblock.exercise.controller.dto.FlightListResult
import org.deblock.exercise.dto.FlightResult
import org.deblock.exercise.mock.MockFlightProvider
import org.deblock.exercise.provider.FlightProvider
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FlightSearchMockTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @LocalServerPort
    private var port: Int = 0

    @Test
    fun search_success() {
        val origin = "FRA"
        val destination = "LHR"
        val departureDate = "2025-10-01"
        val returnDate = "2025-10-02"
        val numberOfPassengers = 1

        val response = restTemplate.getForEntity(
            "http://localhost:$port/api/flight/search?origin=$origin&destination=$destination&departureDate=$departureDate&returnDate=$returnDate&numberOfPassengers=$numberOfPassengers",
            FlightListResult::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.flights).hasSize(2)

        val flights = response.body!!.flights
        assertThat(flights[0].fare).isEqualTo(BigDecimal("90.00"))
        assertThat(flights[0].airline).isEqualTo("Airline2")
        assertThat(flights[0].supplier).isEqualTo("mock2")

        assertThat(flights[1].fare).isEqualTo(BigDecimal("100.00"))
        assertThat(flights[1].airline).isEqualTo("Airline1")
        assertThat(flights[1].supplier).isEqualTo("mock1")

        flights.forEach { flight ->
            assertThat(flight.departureAirportCode).isEqualTo("FRA")
            assertThat(flight.destinationAirportCode).isEqualTo("LHR")
            assertThat(flight.departureDate).isEqualTo(LocalDateTime.of(2023, 10, 1, 10, 0))
            assertThat(flight.arrivalDate).isEqualTo(LocalDateTime.of(2023, 10, 1, 12, 0))
        }
    }

    @Test
    fun search_invalidOrigin_returnsBadRequest() {
        val invalidOrigin = "FR"
        val destination = "LHR"
        val departureDate = "2025-10-01"
        val returnDate = "2025-10-02"
        val numberOfPassengers = 1

        val response = restTemplate.getForEntity(
            "http://localhost:$port/api/flight/search?origin=$invalidOrigin&destination=$destination&departureDate=$departureDate&returnDate=$returnDate&numberOfPassengers=$numberOfPassengers",
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun search_invalidReturnDate_returnsBadRequest() {
        val origin = "FRA"
        val destination = "LHR"
        val departureDate = "2025-10-02"
        val returnDate = "2025-10-01"
        val numberOfPassengers = 1

        val response = restTemplate.getForEntity(
            "http://localhost:$port/api/flight/search?origin=$origin&destination=$destination&departureDate=$departureDate&returnDate=$returnDate&numberOfPassengers=$numberOfPassengers",
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @TestConfiguration
    class TestConfig {

        @Bean
        fun mock1FlightProvider(): FlightProvider {
            return MockFlightProvider(
                mockResults = listOf(
                    FlightResult(
                        airline = "Airline1",
                        supplier = "mock1",
                        fare = BigDecimal("100.00"),
                        departureAirportCode = "FRA",
                        destinationAirportCode = "LHR",
                        departureDate = LocalDateTime.of(2023, 10, 1, 10, 0),
                        arrivalDate = LocalDateTime.of(2023, 10, 1, 12, 0)
                    )
                )
            )
        }

        @Bean
        fun mock2FlightProvider(): FlightProvider {
            return MockFlightProvider(
                mockResults = listOf(
                    FlightResult(
                        airline = "Airline2",
                        supplier = "mock2",
                        fare = BigDecimal("90.00"),
                        departureAirportCode = "FRA",
                        destinationAirportCode = "LHR",
                        departureDate = LocalDateTime.of(2023, 10, 1, 10, 0),
                        arrivalDate = LocalDateTime.of(2023, 10, 1, 12, 0)
                    )
                )
            )
        }
    }
}
