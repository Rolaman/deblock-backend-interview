package org.deblock.exercise.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "app")
data class AppProps(
    val crazyAirHost: String,
    val toughJetHost: String,
    val useCrazyAir: Boolean = true,
    val useToughJet: Boolean = true,
    val timeoutDuration: Duration = Duration.ofSeconds(30),
    val maxResults: Int = 50,
    val retryAttempts: Int = 3,
)