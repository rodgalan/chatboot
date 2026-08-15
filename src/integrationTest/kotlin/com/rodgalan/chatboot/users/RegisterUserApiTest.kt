package com.rodgalan.chatboot.users

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.web.client.RestClient
import tools.jackson.databind.JsonNode

@SpringBootTest
@AutoConfigureMockMvc
class RegisterUserApiTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var jdbcClient: JdbcClient

    private val mailpitClient = RestClient.create("http://localhost:8025")

    @Test
    fun `registers a new user and persists it as non validated`() {
        val email = "user-${UUID.randomUUID()}@example.com"

        mockMvc.post("/api/v1/users") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"$email","password":"Sup3r!Secret123"}"""
        }.andExpect {
            status { isCreated() }
        }

        val persisted = jdbcClient.sql("SELECT status, role FROM users WHERE email = :email")
            .param("email", email)
            .query { rs, _ -> rs.getString("status") to rs.getString("role") }
            .single()

        assertEquals("NON_VALIDATED" to "USER", persisted)
    }

    @Test
    fun `delivers a verification email to the smtp server`() {
        val email = "user-${UUID.randomUUID()}@example.com"

        mockMvc.post("/api/v1/users") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"$email","password":"Sup3r!Secret123"}"""
        }.andExpect {
            status { isCreated() }
        }

        val searchResult = mailpitClient.get()
            .uri("/api/v1/search?query=to:{email}", email)
            .retrieve()
            .body(JsonNode::class.java)

        assertEquals(1, searchResult?.get("messages_count")?.asInt())
        assertEquals(
            "Confirm your email address",
            searchResult?.get("messages")?.get(0)?.get("Subject")?.asString(),
        )
        assertTrue(
            searchResult?.get("messages")?.get(0)?.get("Snippet")?.asString()
                ?.contains("verify-email?token=") == true,
        )
    }

}
