package com.example.offers.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OfferRepository : JpaRepository<OfferEntity, UUID> {
    fun findAllByActiveTrue(): List<OfferEntity>
}
