package com.example.offers.api

import com.example.offers.domain.Offer
import com.example.offers.service.OfferFeed
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CreateOfferRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val title: String,

    @field:DecimalMin("0.00")
    @field:DecimalMax("100.00")
    val cashbackPercent: BigDecimal,

    @field:Min(0)
    @field:Max(10_000)
    val priority: Int = 0,
)

data class OfferResponse(
    val id: UUID,
    val title: String,
    val cashbackPercent: BigDecimal,
    val priority: Int,
    val createdAt: Instant,
) {
    companion object {
        fun from(offer: Offer): OfferResponse =
            OfferResponse(
                id = offer.id,
                title = offer.title,
                cashbackPercent = offer.cashbackPercent,
                priority = offer.priority,
                createdAt = offer.createdAt,
            )
    }
}

data class OfferFeedResponse(
    val ranking: String,
    val items: List<OfferResponse>,
) {
    companion object {
        fun from(feed: OfferFeed): OfferFeedResponse =
            OfferFeedResponse(
                ranking = feed.rankingVersion.apiValue,
                items = feed.offers.map(OfferResponse::from),
            )
    }
}
