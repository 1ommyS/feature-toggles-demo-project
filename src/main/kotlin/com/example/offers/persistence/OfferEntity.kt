package com.example.offers.persistence

import com.example.offers.domain.Offer
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "offers")
class OfferEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false, length = 200)
    var title: String = "",

    @Column(name = "cashback_percent", nullable = false, precision = 5, scale = 2)
    var cashbackPercent: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var priority: Int = 0,

    @Column(nullable = false)
    var active: Boolean = true,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
) {
    fun toDomain(): Offer =
        Offer(
            id = id,
            title = title,
            cashbackPercent = cashbackPercent,
            priority = priority,
            active = active,
            createdAt = createdAt,
        )
}
