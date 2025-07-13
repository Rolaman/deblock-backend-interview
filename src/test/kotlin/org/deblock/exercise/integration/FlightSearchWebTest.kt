package org.deblock.exercise.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.assertj.core.api.Assertions.assertThat
import org.deblock.exercise.controller.dto.FlightListResult
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.math.BigDecimal

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FlightSearchWebTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @LocalServerPort
    private var port: Int = 0

    companion object {
        private lateinit var crazyAirServer: WireMockServer
        private lateinit var toughJetServer: WireMockServer

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            crazyAirServer = WireMockServer(wireMockConfig().port(8085))
            toughJetServer = WireMockServer(wireMockConfig().port(8086))
            
            crazyAirServer.start()
            toughJetServer.start()

            registry.add("app.use-crazy-air") { true }
            registry.add("app.use-tough-jet") { true }
            registry.add("app.crazy-air-host") { "http://localhost:${crazyAirServer.port()}/crazy-air" }
            registry.add("app.tough-jet-host") { "http://localhost:${toughJetServer.port()}/tough-jet" }
        }

        @JvmStatic
        @AfterAll
        fun stopServers() {
            crazyAirServer.stop()
            toughJetServer.stop()
        }
    }

    @BeforeEach
    fun setUp() {
        crazyAirServer.resetAll()
        toughJetServer.resetAll()
        setupCrazyAirMocks()
        setupToughJetMocks()
    }

    @AfterEach
    fun tearDown() {
        crazyAirServer.resetAll()
        toughJetServer.resetAll()
    }

    @Test
    fun search_success_withRealApiSimulation() {
        val origin = "FRA"
        val destination = "LHR"
        val departureDate = "2025-10-01"
        val returnDate = "2025-10-02"
        val numberOfPassengers = 1

        val response = restTemplate.getForEntity(
            "http://localhost:$port/api/flight/search?origin=$origin&destination=$destination&departureDate=$departureDate&returnDate=$returnDate&numberOfPassengers=$numberOfPassengers",
            FlightListResult::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.flights).hasSize(2)

        val flights = response.body!!.flights
        
        assertThat(flights[0].fare).isLessThanOrEqualTo(flights[1].fare)
        
        val suppliers = flights.map { it.supplier }.toSet()
        assertThat(suppliers).contains("CrazyAir", "ToughJet")

        flights.forEach { flight ->
            assertThat(flight.departureAirportCode).isEqualTo("FRA")
            assertThat(flight.destinationAirportCode).isEqualTo("LHR")
            assertThat(flight.airline).isNotBlank()
            assertThat(flight.fare).isGreaterThan(BigDecimal.ZERO)
        }
    }

    @Test
    fun search_success_apiFailure_gracefulDegradation() {
        crazyAirServer.stubFor(
            post(urlEqualTo("/crazy-air/search"))
                .willReturn(serverError())
        )

        val origin = "FRA"
        val destination = "LHR"
        val departureDate = "2025-10-01"
        val returnDate = "2025-10-02"
        val numberOfPassengers = 1

        val response = restTemplate.getForEntity(
            "http://localhost:$port/api/flight/search?origin=$origin&destination=$destination&departureDate=$departureDate&returnDate=$returnDate&numberOfPassengers=$numberOfPassengers",
            FlightListResult::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.flights).hasSize(1)
        assertThat(response.body!!.flights[0].supplier).isEqualTo("ToughJet")
    }

    private fun setupCrazyAirMocks() {
        crazyAirServer.stubFor(
            post(urlEqualTo("/crazy-air/search"))
                .withRequestBody(matchingJsonPath("$.origin", equalTo("FRA")))
                .withRequestBody(matchingJsonPath("$.destination", equalTo("LHR")))
                .withRequestBody(matchingJsonPath("$.departureDate", equalTo("2025-10-01")))
                .withRequestBody(matchingJsonPath("$.returnDate", equalTo("2025-10-02")))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            [
                                {
                                    "airline": "Lufthansa",
                                    "price": 150.50,
                                    "cabinclass": "E",
                                    "departureAirportCode": "FRA",
                                    "destinationAirportCode": "LHR",
                                    "departureDate": "2025-10-01T10:00:00",
                                    "arrivalDate": "2025-10-01T12:00:00"
                                }
                            ]
                            """.trimIndent()
                        )
                )
        )
    }

    private fun setupToughJetMocks() {
        toughJetServer.stubFor(
            post(urlEqualTo("/tough-jet/search"))
                .withRequestBody(matchingJsonPath("$.from", equalTo("FRA")))
                .withRequestBody(matchingJsonPath("$.to", equalTo("LHR")))
                .withRequestBody(matchingJsonPath("$.outboundDate", equalTo("2025-10-01")))
                .withRequestBody(matchingJsonPath("$.inboundDate", equalTo("2025-10-02")))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            [
                                {
                                    "carrier": "British Airways",
                                    "basePrice": 120.00,
                                    "tax": 25.00,
                                    "discount": 5.00,
                                    "departureAirportName": "FRA",
                                    "arrivalAirportName": "LHR",
                                    "outboundDateTime": "2025-10-01T10:00:00Z",
                                    "inboundDateTime": "2025-10-01T12:00:00Z"
                                }
                            ]
                            """.trimIndent()
                        )
                )
        )
    }
}
