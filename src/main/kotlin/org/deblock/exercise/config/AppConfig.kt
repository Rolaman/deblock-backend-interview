package org.deblock.exercise.config

import org.deblock.exercise.client.crazyair.CrazyAirClient
import org.deblock.exercise.client.toughjet.ToughJetClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient


@Configuration
@EnableConfigurationProperties(AppProps::class)
class AppConfig(
    private val props: AppProps,
) {

    @Bean
    @ConditionalOnBooleanProperty("app.use-crazy-air")
    fun crazyAirClient(): CrazyAirClient {
        val restClient = RestClient.builder()
            .baseUrl(props.crazyAirHost)
            .requestFactory(getClientHttpRequestFactory())
            .build()
        return CrazyAirClient(restClient)
    }

    @Bean
    @ConditionalOnBooleanProperty("app.use-tough-jet")
    fun toughJetClient(): ToughJetClient {
        val restClient = RestClient.builder()
            .baseUrl(props.toughJetHost)
            .requestFactory(getClientHttpRequestFactory())
            .build()
        return ToughJetClient(restClient)
    }

    private fun getClientHttpRequestFactory(): ClientHttpRequestFactory {
        val factory = SimpleClientHttpRequestFactory()
        factory.setReadTimeout(props.timeoutDuration)
        factory.setConnectTimeout(props.timeoutDuration)
        return factory
    }
}
