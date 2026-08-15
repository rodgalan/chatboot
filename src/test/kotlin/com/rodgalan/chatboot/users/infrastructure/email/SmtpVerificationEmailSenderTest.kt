package com.rodgalan.chatboot.users.infrastructure.email

import com.rodgalan.chatboot.users.domain.Email
import com.rodgalan.chatboot.users.domain.VerificationToken
import com.rodgalan.chatboot.users.infrastructure.config.EmailVerificationProperties
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

class SmtpVerificationEmailSenderTest {

    private val mailSender = mockk<JavaMailSender>()
    private val properties = EmailVerificationProperties(
        from = "no-reply@chatboot.local",
        verificationUrlTemplate = "http://localhost:3000/verify-email?token={token}",
        tokenTtl = Duration.ofHours(24),
    )
    private val sender = SmtpVerificationEmailSender(mailSender, properties)

    @Test
    fun `sends an email with the verification link built from the configured template`() {
        val messageSlot = slot<SimpleMailMessage>()
        every { mailSender.send(capture(messageSlot)) } returns Unit

        sender.send(Email.of("user@example.com"), VerificationToken("abc123"))

        assertEquals("no-reply@chatboot.local", messageSlot.captured.from)
        assertEquals("user@example.com", messageSlot.captured.to?.single())
        assertEquals("Confirm your email address", messageSlot.captured.subject)
        assertTrue(messageSlot.captured.text?.contains("http://localhost:3000/verify-email?token=abc123") == true)
    }

}
