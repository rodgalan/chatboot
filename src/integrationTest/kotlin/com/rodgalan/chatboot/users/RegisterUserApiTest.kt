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
class RegisterUserApiTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var jdbcClient: JdbcClient

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

}
