package org.deblock.exercise.client.toughjet

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.math.BigDecimal
import java.time.Instant

data class ToughJetFlight(
    @field:NotBlank(message = "Carrier name cannot be blank")
    val carrier: String,
    @field:DecimalMin(value = "0.0", inclusive = true, message = "Base price must be zero or greater")
    val basePrice: BigDecimal,
    @field:DecimalMin(value = "0.0", inclusive = true, message = "Tax must be zero or greater")
    val tax: BigDecimal,
    @field:DecimalMin(value = "0.0", inclusive = true, message = "Discount must be zero or greater")
    @field:DecimalMax(value = "100.0", inclusive = true, message = "Discount cannot exceed 100%")
    val discount: BigDecimal,
    @field:Pattern(regexp = "^[A-Z]{3}$", message = "Departure airport name must be a 3-letter IATA code")
    val departureAirportName: String,
    @field:Pattern(regexp = "^[A-Z]{3}$", message = "Arrival airport name must be a 3-letter IATA code")
    val arrivalAirportName: String,
    val outboundDateTime: Instant,
    val inboundDateTime: Instant,
)

data class ToughJetSearchRequest(
    val from: String,
    val to: String,
    val outboundDate: String,
    val inboundDate: String,
    val numberOfAdults: Int,
)
