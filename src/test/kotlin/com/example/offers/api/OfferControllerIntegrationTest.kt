package com.example.offers.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(
    properties = [
        "feature-flags.provider=local",
        "feature-flags.local.offers.smart-ranking-v2=true",
    ],
)
@AutoConfigureMockMvc
@Testcontainers
class OfferControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `returns seeded offers using smart ranking`() {
        mockMvc.get("/api/v1/offers") {
            param("userId", "integration-user")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.ranking") { value("smart-v2") }
                jsonPath("$.items.length()") { value(3) }
                jsonPath("$.items[0].title") { value("Travel cashback") }
            }
    }

    companion object {
        @Container
        @ServiceConnection
        @JvmField
        val postgres = PostgreSQLContainer<Nothing>("postgres:16.8-alpine")
    }
}
