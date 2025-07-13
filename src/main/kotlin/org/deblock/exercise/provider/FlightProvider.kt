package org.deblock.exercise.provider

import org.deblock.exercise.dto.FlightResult
import org.deblock.exercise.dto.FlightSearchRequest

interface FlightProvider {
    fun search(request: FlightSearchRequest): List<FlightResult>
}
