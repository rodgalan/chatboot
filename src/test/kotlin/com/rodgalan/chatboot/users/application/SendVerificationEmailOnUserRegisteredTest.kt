package com.rodgalan.chatboot.users.application

import com.rodgalan.chatboot.users.domain.Email
import com.rodgalan.chatboot.users.domain.EmailVerificationToken
import com.rodgalan.chatboot.users.domain.EmailVerificationTokenRepository
import com.rodgalan.chatboot.users.domain.UserId
import com.rodgalan.chatboot.users.domain.UserRegistered
import com.rodgalan.chatboot.users.domain.VerificationEmailSender
import com.rodgalan.chatboot.users.domain.VerificationToken
import com.rodgalan.chatboot.users.domain.VerificationTokenGenerator
import com.rodgalan.chatboot.users.infrastructure.config.EmailVerificationProperties
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.time.Duration
import java.time.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SendVerificationEmailOnUserRegisteredTest {

    private val tokenGenerator = mockk<VerificationTokenGenerator>()
    private val tokenRepository = mockk<EmailVerificationTokenRepository>()
    private val emailSender = mockk<VerificationEmailSender>()
    private val properties = EmailVerificationProperties(
        from = "no-reply@chatboot.local",
        verificationUrlTemplate = "http://localhost:3000/verify-email?token={token}",
        tokenTtl = Duration.ofHours(24),
    )
    private lateinit var sendVerificationEmailOnUserRegistered: SendVerificationEmailOnUserRegistered

    private val userId = UserId.generate()
    private val event = UserRegistered(userId = userId.value.toString(), email = "user@example.com", occurredOn = Instant.now())

    @BeforeTest
    fun setUp() {
        sendVerificationEmailOnUserRegistered =
            SendVerificationEmailOnUserRegistered(tokenGenerator, tokenRepository, emailSender, properties)
        every { tokenGenerator.generate() } returns VerificationToken("generated-token")
        every { tokenRepository.save(any()) } returns Unit
        every { emailSender.send(any(), any()) } returns Unit
    }

    @Test
    fun `stores a verification token with the configured expiration`() {
        val tokenSlot = slot<EmailVerificationToken>()
        every { tokenRepository.save(capture(tokenSlot)) } returns Unit

        sendVerificationEmailOnUserRegistered.on(event)

        assertEquals(userId, tokenSlot.captured.userId)
        val expectedExpiry = Instant.now().plus(properties.tokenTtl)
        assertTrue(tokenSlot.captured.expiresAt.isAfter(expectedExpiry.minusSeconds(5)))
        assertTrue(tokenSlot.captured.expiresAt.isBefore(expectedExpiry.plusSeconds(5)))
    }

    @Test
    fun `sends the verification email to the registered address`() {
        val emailSlot = slot<Email>()
        val tokenSlot = slot<VerificationToken>()
        every { emailSender.send(capture(emailSlot), capture(tokenSlot)) } returns Unit

        sendVerificationEmailOnUserRegistered.on(event)

        assertEquals(Email.of("user@example.com"), emailSlot.captured)
        assertEquals(VerificationToken("generated-token"), tokenSlot.captured)
    }

}
