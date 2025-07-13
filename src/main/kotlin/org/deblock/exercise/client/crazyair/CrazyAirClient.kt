package org.deblock.exercise.client.crazyair

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validator
import org.deblock.exercise.dto.FlightSearchRequest
import org.slf4j.LoggerFactory
import org.springframework.web.client.RestClient
import java.time.format.DateTimeFormatter

class CrazyAirClient(
    private val restClient: RestClient,
    private val validator: Validator
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun searchFlights(searchRequest: FlightSearchRequest): List<CrazyAirFlight> {
        val crazyAirRequest = CrazyAirSearchRequest(
            origin = searchRequest.origin,
            destination = searchRequest.destination,
            departureDate = searchRequest.departureDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            returnDate = searchRequest.returnDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            passengerCount = searchRequest.numberOfPassengers,
        )

        return runCatching {
            val flights = restClient.post()
                .uri("/search")
                .body(crazyAirRequest)
                .retrieve()
                .body(Array<CrazyAirFlight>::class.java)
                ?.toList() ?: emptyList()

            validateFlights(flights)
        }.getOrElse { exception ->
            logger.error("Failed to search flights from CrazyAir", exception)
            emptyList()
        }
    }

    private fun validateFlights(flights: List<CrazyAirFlight>): List<CrazyAirFlight> {
        return flights.filter { flight ->
            val violations: Set<ConstraintViolation<CrazyAirFlight>> = validator.validate(flight)
            if (violations.isNotEmpty()) {
                val errorMessages = violations.joinToString(", ") { it.message }
                logger.warn("Invalid CrazyAir flight data: $errorMessages. Flight: $flight")
                false
            } else {
                true
            }
        }
    }
}
