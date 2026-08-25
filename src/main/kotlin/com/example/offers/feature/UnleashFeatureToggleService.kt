package com.example.offers.feature

import io.getunleash.Unleash
import io.getunleash.UnleashContext
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "feature-flags", name = ["provider"], havingValue = "unleash")
class UnleashFeatureToggleService(
    private val unleash: Unleash,
) : FeatureToggleService {

    override fun isEnabled(toggle: FeatureToggle, context: FeatureToggleContext): Boolean {
        val builder = UnleashContext.builder()

        context.userId?.let { builder.userId(it) }
        context.properties.forEach { (name, value) -> builder.addProperty(name, value) }

        return unleash.isEnabled(toggle.key, builder.build(), false)
    }
}
