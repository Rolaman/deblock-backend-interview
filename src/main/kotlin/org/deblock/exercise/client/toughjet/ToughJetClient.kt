package org.deblock.exercise.client.toughjet

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validator
import org.deblock.exercise.dto.FlightSearchRequest
import org.slf4j.LoggerFactory
import org.springframework.web.client.RestClient

class ToughJetClient(
    private val restClient: RestClient,
    private val validator: Validator
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun searchFlights(searchRequest: FlightSearchRequest): List<ToughJetFlight> {
        val toughJetRequest = ToughJetSearchRequest(
            from = searchRequest.origin,
            to = searchRequest.destination,
            outboundDate = searchRequest.departureDate.toString(),
            inboundDate = searchRequest.returnDate.toString(),
            numberOfAdults = searchRequest.numberOfPassengers,
        )

        return runCatching {
            val flights = restClient.post()
                .uri("/search")
                .body(toughJetRequest)
                .retrieve()
                .body(Array<ToughJetFlight>::class.java)
                ?.toList() ?: emptyList()

            validateFlights(flights)
        }.getOrElse { exception ->
            logger.error("Failed to search flights from ToughJet", exception)
            emptyList()
        }
    }

    private fun validateFlights(flights: List<ToughJetFlight>): List<ToughJetFlight> {
        return flights.filter { flight ->
            val violations: Set<ConstraintViolation<ToughJetFlight>> = validator.validate(flight)
            if (violations.isNotEmpty()) {
                val errorMessages = violations.joinToString(", ") { it.message }
                logger.warn("Invalid ToughJet flight data: $errorMessages. Flight: $flight")
                false
            } else {
                true
            }
        }
    }
}
