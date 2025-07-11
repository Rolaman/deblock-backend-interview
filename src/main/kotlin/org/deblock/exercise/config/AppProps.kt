package org.deblock.exercise.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProps(
    val crazyAirHost: String,
    val toughJetHost: String,
)