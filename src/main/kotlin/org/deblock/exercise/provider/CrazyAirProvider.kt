package org.deblock.exercise.provider

import kotlinx.coroutines.runBlocking
import org.deblock.exercise.client.crazyair.CrazyAirClient
import org.deblock.exercise.client.crazyair.CrazyAirFlight
import org.deblock.exercise.dto.FlightResult
import org.deblock.exercise.dto.FlightSearchRequest
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.stereotype.Component
import java.math.RoundingMode

@Component
@ConditionalOnBooleanProperty("app.use-crazy-air")
class CrazyAirProvider(
    private val crazyAirClient: CrazyAirClient,
) : FlightProvider {

    override fun search(request: FlightSearchRequest): List<FlightResult> = runBlocking {
        crazyAirClient.searchFlights(request)
            .map { map(it) }
    }

    private fun map(crazyAirFlight: CrazyAirFlight): FlightResult {
        return FlightResult(
            airline = crazyAirFlight.airline,
            supplier = SUPPLIER_NAME,
            fare = crazyAirFlight.price.setScale(2, RoundingMode.HALF_UP),
            departureAirportCode = crazyAirFlight.departureAirportCode,
            destinationAirportCode = crazyAirFlight.destinationAirportCode,
            departureDate = crazyAirFlight.departureDate,
            arrivalDate = crazyAirFlight.arrivalDate
        )
    }

    companion object {
        const val SUPPLIER_NAME = "CrazyAir"
    }
}
