package com.example.offers.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "feature-flags")
data class FeatureFlagProperties(
    var provider: String = "local",
    var local: Local = Local(),
    var unleash: Unleash = Unleash(),
) {
    data class Local(
        var offers: Offers = Offers(),
    )

    data class Offers(
        var smartRankingV2: Boolean = false,
    )

    data class Unleash(
        var url: String = "http://localhost:4242/api",
        var token: String = "default:development.unleash-insecure-api-token",
        var appName: String = "offer-service",
        var instanceId: String = "local",
    )
}
