package com.rodgalan.chatboot.users.infrastructure.http

import com.rodgalan.chatboot.users.application.VerifyUserEmail
import com.rodgalan.chatboot.users.domain.VerificationTokenAlreadyConsumedError
import com.rodgalan.chatboot.users.domain.VerificationTokenExpiredError
import com.rodgalan.chatboot.users.domain.VerificationTokenNotFoundError
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class EmailVerificationPostControllerTest {

    private val verifyUserEmail = mockk<VerifyUserEmail>()
    private lateinit var mockMvc: MockMvc

    @BeforeTest
    fun setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(EmailVerificationPostController(verifyUserEmail))
            .setControllerAdvice(UsersApiExceptionHandler())
            .build()
    }

    @Test
    fun `returns 204 when the token is valid`() {
        every { verifyUserEmail.verify(any()) } returns Unit

        mockMvc.post("/api/v1/users/email-verifications") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"token":"valid-token"}"""
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `returns 404 when the token does not exist`() {
        every { verifyUserEmail.verify(any()) } throws VerificationTokenNotFoundError("unknown-token")

        mockMvc.post("/api/v1/users/email-verifications") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"token":"unknown-token"}"""
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `returns 410 when the token has expired`() {
        every { verifyUserEmail.verify(any()) } throws VerificationTokenExpiredError("expired-token")

        mockMvc.post("/api/v1/users/email-verifications") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"token":"expired-token"}"""
        }.andExpect {
            status { isGone() }
        }
    }

    @Test
    fun `returns 410 when the token has already been consumed`() {
        every { verifyUserEmail.verify(any()) } throws VerificationTokenAlreadyConsumedError("consumed-token")

        mockMvc.post("/api/v1/users/email-verifications") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"token":"consumed-token"}"""
        }.andExpect {
            status { isGone() }
        }
    }

    @Test
    fun `returns 400 when the request body is missing required fields`() {
        mockMvc.post("/api/v1/users/email-verifications") {
            contentType = MediaType.APPLICATION_JSON
            content = """{}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

}
