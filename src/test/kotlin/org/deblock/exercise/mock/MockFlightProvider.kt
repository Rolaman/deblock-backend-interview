package org.deblock.exercise.mock

import org.deblock.exercise.dto.FlightResult
import org.deblock.exercise.dto.FlightSearchRequest
import org.deblock.exercise.provider.FlightProvider

class MockFlightProvider(
    private val mockResults: List<FlightResult> = emptyList()
) : FlightProvider {

    override fun search(request: FlightSearchRequest): List<FlightResult> {
        return mockResults
    }
}
