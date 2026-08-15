package com.rodgalan.chatboot.users

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
class VerifyUserEmailApiTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var jdbcClient: JdbcClient

    @Test
    fun `verifies a registered user email and activates the account`() {
        val email = "user-${UUID.randomUUID()}@example.com"

        mockMvc.post("/api/v1/users") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"$email","password":"Sup3r!Secret123"}"""
        }.andExpect {
            status { isCreated() }
        }

        val token = jdbcClient.sql(
            """
            SELECT t.token
            FROM email_verification_tokens t
            JOIN users u ON u.id = t.user_id
            WHERE u.email = :email
            """.trimIndent(),
        )
            .param("email", email)
            .query(String::class.java)
            .single()

        mockMvc.post("/api/v1/users/email-verifications") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"token":"$token"}"""
        }.andExpect {
            status { isNoContent() }
        }

        val status = jdbcClient.sql("SELECT status FROM users WHERE email = :email")
            .param("email", email)
            .query(String::class.java)
            .single()

        assertEquals("ACTIVE", status)
    }

}
