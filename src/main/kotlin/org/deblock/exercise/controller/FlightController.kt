package org.deblock.exercise.controller

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import org.deblock.exercise.controller.dto.FlightListResult
import org.deblock.exercise.dto.FlightSearchRequest
import org.deblock.exercise.service.FlightService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/flight")
@Validated
class FlightController(
    private val flightService: FlightService,
) {

    @GetMapping("/search")
    fun search(
        @RequestParam
        @Pattern(regexp = "^[A-Z]{3}$", message = "Origin must be a 3-letter IATA code")
        origin: String,
        @RequestParam
        @Pattern(regexp = "^[A-Z]{3}$", message = "Destination must be a 3-letter IATA code")
        destination: String,
        @RequestParam
        departureDate: LocalDate,
        @RequestParam
        returnDate: LocalDate,
        @RequestParam
        @Min(value = 1, message = "Number of passengers must be at least 1")
        @Max(value = 4, message = "Number of passengers cannot exceed 4")
        numberOfPassengers: Int,
    ): FlightListResult {
        if (returnDate.isBefore(departureDate)) {
            throw IllegalArgumentException("Return date must be after departure date")
        }
        if (origin == destination) {
            throw IllegalArgumentException("Origin and destination cannot be the same")
        }

        val results = flightService.search(
            FlightSearchRequest(
                origin = origin,
                destination = destination,
                departureDate = departureDate,
                returnDate = returnDate,
                numberOfPassengers = numberOfPassengers
            )
        )
        return FlightListResult(results)
    }
}
