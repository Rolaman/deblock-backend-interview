package org.deblock.exercise.service

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.deblock.exercise.dto.FlightResult
import org.deblock.exercise.dto.FlightSearchRequest
import org.deblock.exercise.provider.FlightProvider
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service

@Service
class FlightService(
    private val providers: List<FlightProvider>,
) {

    fun search(request: FlightSearchRequest): List<FlightResult> = runBlocking {
        providers.map { provider ->
            async {
                runCatching { provider.search(request) }
                    .onFailure {
                        getLogger(this::class.java).error("Error searching flights with provider: ${provider::class.simpleName}", it)
                    }
                    .getOrElse { emptyList() }
            }
        }.awaitAll()
            .flatten()
            .sortedBy { it.fare }
    }
}
