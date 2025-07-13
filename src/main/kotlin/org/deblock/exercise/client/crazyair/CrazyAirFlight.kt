package org.deblock.exercise.client.crazyair

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.math.BigDecimal
import java.time.LocalDateTime

data class CrazyAirFlight(
    @field:NotBlank(message = "Airline name cannot be blank")
    val airline: String,
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    val price: BigDecimal,
    val cabinclass: CabinClass,
    @field:Pattern(regexp = "^[A-Z]{3}$", message = "Departure airport code must be a 3-letter IATA code")
    val departureAirportCode: String,
    @field:Pattern(regexp = "^[A-Z]{3}$", message = "Destination airport code must be a 3-letter IATA code")
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
