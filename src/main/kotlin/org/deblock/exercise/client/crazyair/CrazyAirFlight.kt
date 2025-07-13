package org.deblock.exercise.client.crazyair

import java.math.BigDecimal
import java.time.LocalDateTime

data class CrazyAirFlight(
    val airline: String,
    val price: BigDecimal,
    val cabinclass: CabinClass,
    val departureAirportCode: String,
    val destinationAirportCode: String,
    val departureDate: LocalDateTime,
    val arrivalDate: LocalDateTime,
)

data class CrazyAirSearchRequest(
    val origin: String,
    val destination: String,
    val departureDate: String,
    val returnDate: String,
    val passengerCount: Int,
)

enum class CabinClass {
    E, // Economy
    B, // Business
}