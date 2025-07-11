package org.deblock.exercise.client.toughjet

import org.deblock.exercise.dto.FlightSearchRequest
import org.springframework.web.client.RestClient

class ToughJetClient(
    private val restClient: RestClient
) {

    fun searchFlights(searchRequest: FlightSearchRequest): List<ToughJetFlight> {
        val toughJetRequest = ToughJetSearchRequest(
            from = searchRequest.origin,
            to = searchRequest.destination,
            outboundDate = searchRequest.departureDate.toString(),
            inboundDate = searchRequest.returnDate.toString(),
            numberOfAdults = searchRequest.numberOfPassengers,
        )

        return restClient.post()
            .uri("/search")
            .body(toughJetRequest)
            .retrieve()
            .body(Array<ToughJetFlight>::class.java)
            ?.toList() ?: emptyList()
    }
}