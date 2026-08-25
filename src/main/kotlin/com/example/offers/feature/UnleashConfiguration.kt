package com.example.offers.feature

import com.example.offers.config.FeatureFlagProperties
import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(prefix = "feature-flags", name = ["provider"], havingValue = "unleash")
class UnleashConfiguration {

    @Bean(destroyMethod = "shutdown")
    fun unleash(properties: FeatureFlagProperties): Unleash {
        val config = UnleashConfig.builder()
            .appName(properties.unleash.appName)
            .instanceId(properties.unleash.instanceId)
            .unleashAPI(properties.unleash.url)
            .apiKey(properties.unleash.token)
            .build()

        return DefaultUnleash(config)
    }
}
