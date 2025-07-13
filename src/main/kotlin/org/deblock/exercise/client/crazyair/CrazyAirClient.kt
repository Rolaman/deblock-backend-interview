package org.deblock.exercise.client.crazyair

import org.deblock.exercise.dto.FlightSearchRequest
import org.springframework.web.client.RestClient
import java.time.format.DateTimeFormatter

class CrazyAirClient(
    private val restClient: RestClient
) {

    suspend fun searchFlights(searchRequest: FlightSearchRequest): List<CrazyAirFlight> {
        val crazyAirRequest = CrazyAirSearchRequest(
            origin = searchRequest.origin,
            destination = searchRequest.destination,
            departureDate = searchRequest.departureDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            returnDate = searchRequest.returnDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            passengerCount = searchRequest.numberOfPassengers,
        )

        return runCatching {
            restClient.post()
                .uri("/search")
                .body(crazyAirRequest)
                .retrieve()
                .body(Array<CrazyAirFlight>::class.java)
                ?.toList() ?: emptyList()
        }.getOrElse { exception ->
            throw CrazyAirException("Failed to search flights from CrazyAir", exception)
        }
    }
}
