package com.example.offers.feature

interface FeatureToggleService {
    fun isEnabled(toggle: FeatureToggle, context: FeatureToggleContext = FeatureToggleContext()): Boolean
}

data class FeatureToggleContext(
    val userId: String? = null,
    val properties: Map<String, String> = emptyMap(),
)

enum class FeatureToggle(val key: String) {
    SMART_RANKING_V2("offers.smart-ranking-v2"),
}
