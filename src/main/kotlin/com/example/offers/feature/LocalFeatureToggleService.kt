package com.example.offers.feature

import com.example.offers.config.FeatureFlagProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    prefix = "feature-flags",
    name = ["provider"],
    havingValue = "local",
    matchIfMissing = true,
)
class LocalFeatureToggleService(
    private val properties: FeatureFlagProperties,
) : FeatureToggleService {

    override fun isEnabled(toggle: FeatureToggle, context: FeatureToggleContext): Boolean =
        when (toggle) {
            FeatureToggle.SMART_RANKING_V2 -> properties.local.offers.smartRankingV2
        }
}
