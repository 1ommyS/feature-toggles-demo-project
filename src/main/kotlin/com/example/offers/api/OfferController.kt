package com.example.offers.api

import com.example.offers.service.CreateOfferCommand
import com.example.offers.service.OfferService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/offers")
@Validated
class OfferController(
    private val offerService: OfferService,
) {

    @GetMapping
    fun getOffers(
        @RequestParam
        @NotBlank
        @Size(max = 100)
        userId: String,
    ): OfferFeedResponse =
        OfferFeedResponse.from(offerService.getFeed(userId))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createOffer(
        @Valid @RequestBody request: CreateOfferRequest,
    ): OfferResponse =
        OfferResponse.from(
            offerService.createOffer(
                CreateOfferCommand(
                    title = request.title,
                    cashbackPercent = request.cashbackPercent,
                    priority = request.priority,
                ),
            ),
        )
}
