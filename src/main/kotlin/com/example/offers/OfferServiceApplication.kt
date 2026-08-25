package com.example.offers

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class OfferServiceApplication

fun main(args: Array<String>) {
    runApplication<OfferServiceApplication>(*args)
}
