package com.example.offers.service

import com.example.offers.domain.Offer
import com.example.offers.feature.FeatureToggle
import com.example.offers.feature.FeatureToggleContext
import com.example.offers.feature.FeatureToggleService
import com.example.offers.persistence.OfferEntity
import com.example.offers.persistence.OfferRepository
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
class OfferService(
    private val repository: OfferRepository,
    private val featureToggleService: FeatureToggleService,
    private val meterRegistry: MeterRegistry,
) {

    @Transactional(readOnly = true)
    fun getFeed(userId: String): OfferFeed {
        val smartRankingEnabled = featureToggleService.isEnabled(
            FeatureToggle.SMART_RANKING_V2,
            FeatureToggleContext(userId = userId),
        )

        val offers = repository.findAllByActiveTrue()
            .map(OfferEntity::toDomain)
            .let { if (smartRankingEnabled) smartRanking(it) else legacyRanking(it) }

        val ranking = if (smartRankingEnabled) RankingVersion.SMART_V2 else RankingVersion.LEGACY
        meterRegistry.counter("offers.feed.requests", "ranking", ranking.apiValue).increment()

        return OfferFeed(ranking, offers)
    }

    @Transactional
    fun createOffer(command: CreateOfferCommand): Offer =
        repository.save(
            OfferEntity(
                id = UUID.randomUUID(),
                title = command.title.trim(),
                cashbackPercent = command.cashbackPercent,
                priority = command.priority,
                active = true,
            ),
        ).toDomain()

    private fun legacyRanking(offers: List<Offer>): List<Offer> =
        offers.sortedByDescending(Offer::createdAt)

    private fun smartRanking(offers: List<Offer>): List<Offer> =
        offers.sortedWith(
            compareByDescending<Offer> { it.priority }
                .thenByDescending { it.cashbackPercent }
                .thenByDescending { it.createdAt },
        )
}

data class OfferFeed(
    val rankingVersion: RankingVersion,
    val offers: List<Offer>,
)

enum class RankingVersion(val apiValue: String) {
    LEGACY("legacy"),
    SMART_V2("smart-v2"),
}

data class CreateOfferCommand(
    val title: String,
    val cashbackPercent: BigDecimal,
    val priority: Int,
)
