package com.rodgalan.chatboot.users.infrastructure.http

import com.rodgalan.chatboot.users.application.RegisterUser
import com.rodgalan.chatboot.users.domain.EmailAlreadyRegisteredError
import com.rodgalan.chatboot.users.domain.InvalidEmailFormatError
import com.rodgalan.chatboot.users.domain.WeakPasswordError
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class UserPostControllerTest {

    private val registerUser = mockk<RegisterUser>()
    private lateinit var mockMvc: MockMvc

    @BeforeTest
    fun setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(UserPostController(registerUser))
            .setControllerAdvice(UsersApiExceptionHandler())
            .build()
    }

    @Test
    fun `returns 201 when the registration request is valid`() {
        every { registerUser.register(any()) } returns Unit

        mockMvc.post("/api/v1/users") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"user@example.com","password":"Sup3r!Secret123"}"""
        }.andExpect {
            status { isCreated() }
        }
    }

    @Test
    fun `returns 409 when the email is already registered`() {
        every { registerUser.register(any()) } throws EmailAlreadyRegisteredError("user@example.com")

        mockMvc.post("/api/v1/users") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"user@example.com","password":"Sup3r!Secret123"}"""
        }.andExpect {
            status { isConflict() }
        }
    }

    @Test
    fun `returns 422 when the email format is invalid`() {
        every { registerUser.register(any()) } throws InvalidEmailFormatError("not-an-email")

        mockMvc.post("/api/v1/users") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"not-an-email","password":"Sup3r!Secret123"}"""
        }.andExpect {
            status { isUnprocessableContent() }
        }
    }

    @Test
    fun `returns 422 when the password does not meet the policy`() {
        every { registerUser.register(any()) } throws WeakPasswordError()

        mockMvc.post("/api/v1/users") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"user@example.com","password":"weak"}"""
        }.andExpect {
            status { isUnprocessableContent() }
        }
    }

    @Test
    fun `returns 400 when the request body is missing required fields`() {
        mockMvc.post("/api/v1/users") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"user@example.com"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

}
