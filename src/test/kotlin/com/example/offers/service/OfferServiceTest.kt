package com.example.offers.service

import com.example.offers.feature.FeatureToggle
import com.example.offers.feature.FeatureToggleContext
import com.example.offers.feature.FeatureToggleService
import com.example.offers.persistence.OfferEntity
import com.example.offers.persistence.OfferRepository
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class OfferServiceTest {

    private val repository = mock(OfferRepository::class.java)
    private val featureToggleService = mock(FeatureToggleService::class.java)
    private val meterRegistry = SimpleMeterRegistry()
    private val service = OfferService(repository, featureToggleService, meterRegistry)

    @Test
    fun `uses smart ranking when feature is enabled`() {
        val userId = "user-42"
        `when`(
            featureToggleService.isEnabled(
                FeatureToggle.SMART_RANKING_V2,
                FeatureToggleContext(userId = userId),
            ),
        ).thenReturn(true)

        `when`(repository.findAllByActiveTrue()).thenReturn(
            listOf(
                offer("newer", cashback = "20.00", priority = 1, createdAt = Instant.parse("2026-01-02T00:00:00Z")),
                offer("priority", cashback = "5.00", priority = 100, createdAt = Instant.parse("2026-01-01T00:00:00Z")),
            ),
        )

        val result = service.getFeed(userId)

        assertThat(result.rankingVersion).isEqualTo(RankingVersion.SMART_V2)
        assertThat(result.offers.map { it.title }).containsExactly("priority", "newer")
        verify(featureToggleService).isEnabled(
            FeatureToggle.SMART_RANKING_V2,
            FeatureToggleContext(userId = userId),
        )
    }

    @Test
    fun `uses legacy ranking when feature is disabled`() {
        val userId = "user-7"
        `when`(
            featureToggleService.isEnabled(
                FeatureToggle.SMART_RANKING_V2,
                FeatureToggleContext(userId = userId),
            ),
        ).thenReturn(false)

        `when`(repository.findAllByActiveTrue()).thenReturn(
            listOf(
                offer("older high priority", cashback = "50.00", priority = 100, createdAt = Instant.parse("2026-01-01T00:00:00Z")),
                offer("newer", cashback = "1.00", priority = 1, createdAt = Instant.parse("2026-01-02T00:00:00Z")),
            ),
        )

        val result = service.getFeed(userId)

        assertThat(result.rankingVersion).isEqualTo(RankingVersion.LEGACY)
        assertThat(result.offers.map { it.title }).containsExactly("newer", "older high priority")
    }

    private fun offer(
        title: String,
        cashback: String,
        priority: Int,
        createdAt: Instant,
    ) = OfferEntity(
        id = UUID.randomUUID(),
        title = title,
        cashbackPercent = BigDecimal(cashback),
        priority = priority,
        active = true,
        createdAt = createdAt,
    )
}
