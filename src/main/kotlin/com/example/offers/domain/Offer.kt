package com.example.offers.domain

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class Offer(
    val id: UUID,
    val title: String,
    val cashbackPercent: BigDecimal,
    val priority: Int,
    val active: Boolean,
    val createdAt: Instant,
)
