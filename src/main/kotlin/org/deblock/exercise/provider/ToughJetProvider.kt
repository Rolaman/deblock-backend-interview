package org.deblock.exercise.provider

import kotlinx.coroutines.runBlocking
import org.deblock.exercise.client.toughjet.ToughJetClient
import org.deblock.exercise.client.toughjet.ToughJetFlight
import org.deblock.exercise.dto.FlightResult
import org.deblock.exercise.dto.FlightSearchRequest
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.ZoneOffset

@Component
@ConditionalOnBooleanProperty("app.use-tough-jet")
class ToughJetProvider(
    private val toughJetClient: ToughJetClient,
) : FlightProvider {

    override fun search(request: FlightSearchRequest): List<FlightResult> = runBlocking {
        toughJetClient.searchFlights(request)
            .map { map(it) }
    }

    private fun map(toughJetFlight: ToughJetFlight): FlightResult {
        val fare = calculateFare(
            toughJetFlight.basePrice,
            toughJetFlight.tax,
            toughJetFlight.discount,
        )

        return FlightResult(
            airline = toughJetFlight.carrier,
            supplier = SUPPLIER_NAME,
            fare = fare.setScale(2, RoundingMode.HALF_UP),
            departureAirportCode = toughJetFlight.departureAirportName,
            destinationAirportCode = toughJetFlight.arrivalAirportName,
            departureDate = LocalDateTime.ofInstant(toughJetFlight.outboundDateTime, ZoneOffset.UTC),
            arrivalDate = LocalDateTime.ofInstant(toughJetFlight.inboundDateTime, ZoneOffset.UTC),
        )
    }

    private fun calculateFare(
        basePrice: BigDecimal,
        tax: BigDecimal,
        discountPercentage: BigDecimal,
    ): BigDecimal {
        val priceWithTax = basePrice.add(tax)
        val discountMultiplier = BigDecimal.ONE.subtract(
            discountPercentage.divide(BigDecimal(100), 4, RoundingMode.HALF_UP)
        )
        return priceWithTax.multiply(discountMultiplier)
    }

    companion object {
        const val SUPPLIER_NAME = "ToughJet"
    }
}
