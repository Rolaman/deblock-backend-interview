package org.deblock.exercise.service

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.deblock.exercise.dto.FlightResult
import org.deblock.exercise.dto.FlightSearchRequest
import org.deblock.exercise.provider.FlightProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FlightService(
    private val providers: List<FlightProvider>,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun search(request: FlightSearchRequest): List<FlightResult> = runBlocking {
        providers.map { provider ->
            async {
                runCatching { provider.search(request) }
                    .onFailure { exception ->
                        logger.error(
                            "Error searching flights with provider: ${provider::class.simpleName}",
                            exception
                        )
                    }
                    .getOrElse { emptyList() }
            }
        }.awaitAll()
            .flatten()
            .sortedBy { it.fare }
    }
}
