package com.rodgalan.chatboot.users.application

import com.rodgalan.chatboot.users.domain.DomainEventPublisher
import com.rodgalan.chatboot.users.domain.Email
import com.rodgalan.chatboot.users.domain.EmailVerificationToken
import com.rodgalan.chatboot.users.domain.EmailVerificationTokenRepository
import com.rodgalan.chatboot.users.domain.HashedPassword
import com.rodgalan.chatboot.users.domain.User
import com.rodgalan.chatboot.users.domain.UserAlreadyActiveError
import com.rodgalan.chatboot.users.domain.UserEmailVerified
import com.rodgalan.chatboot.users.domain.UserId
import com.rodgalan.chatboot.users.domain.UserRepository
import com.rodgalan.chatboot.users.domain.UserRole
import com.rodgalan.chatboot.users.domain.UserStatus
import com.rodgalan.chatboot.users.domain.VerificationToken
import com.rodgalan.chatboot.users.domain.VerificationTokenAlreadyConsumedError
import com.rodgalan.chatboot.users.domain.VerificationTokenExpiredError
import com.rodgalan.chatboot.users.domain.VerificationTokenNotFoundError
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class VerifyUserEmailTest {

    private val tokenRepository = mockk<EmailVerificationTokenRepository>()
    private val userRepository = mockk<UserRepository>()
    private val domainEventPublisher = mockk<DomainEventPublisher>()
    private lateinit var verifyUserEmail: VerifyUserEmail

    private val userId = UserId.generate()
    private val nonValidatedUser = User.from(
        id = userId,
        email = Email.of("user@example.com"),
        hashedPassword = HashedPassword("hashed"),
        role = UserRole.USER,
        status = UserStatus.NON_VALIDATED,
        registeredAt = Instant.now(),
    )
    private val validToken = EmailVerificationToken.from(
        token = VerificationToken("valid-token"),
        userId = userId,
        expiresAt = Instant.now().plus(1, ChronoUnit.DAYS),
        consumedAt = null,
    )

    @BeforeTest
    fun setUp() {
        verifyUserEmail = VerifyUserEmail(tokenRepository, userRepository, domainEventPublisher)
        every { tokenRepository.findByToken(VerificationToken("valid-token")) } returns validToken
        every { userRepository.findById(userId) } returns nonValidatedUser
        every { userRepository.save(any()) } returns Unit
        every { tokenRepository.save(any()) } returns Unit
        every { domainEventPublisher.publish(any()) } returns Unit
    }

    @Test
    fun `activates the user account`() {
        val userSlot = slot<User>()
        every { userRepository.save(capture(userSlot)) } returns Unit

        verifyUserEmail.verify(VerifyUserEmailCommand("valid-token"))

        assertEquals(UserStatus.ACTIVE, userSlot.captured.status)
    }

    @Test
    fun `consumes the verification token`() {
        val tokenSlot = slot<EmailVerificationToken>()
        every { tokenRepository.save(capture(tokenSlot)) } returns Unit

        verifyUserEmail.verify(VerifyUserEmailCommand("valid-token"))

        assertTrue(tokenSlot.captured.isConsumed())
    }

    @Test
    fun `publishes a user email verified domain event`() {
        val eventSlot = slot<UserEmailVerified>()
        every { domainEventPublisher.publish(capture(eventSlot)) } returns Unit

        verifyUserEmail.verify(VerifyUserEmailCommand("valid-token"))

        assertEquals(userId.value.toString(), eventSlot.captured.userId)
    }

    @Test
    fun `fails when the token does not exist`() {
        every { tokenRepository.findByToken(VerificationToken("unknown-token")) } returns null

        assertFailsWith<VerificationTokenNotFoundError> {
            verifyUserEmail.verify(VerifyUserEmailCommand("unknown-token"))
        }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `fails when the token has expired`() {
        val expiredToken = EmailVerificationToken.from(
            token = VerificationToken("expired-token"),
            userId = userId,
            expiresAt = Instant.now().minus(1, ChronoUnit.DAYS),
            consumedAt = null,
        )
        every { tokenRepository.findByToken(VerificationToken("expired-token")) } returns expiredToken

        assertFailsWith<VerificationTokenExpiredError> {
            verifyUserEmail.verify(VerifyUserEmailCommand("expired-token"))
        }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `fails when the token has already been consumed`() {
        val consumedToken = EmailVerificationToken.from(
            token = VerificationToken("consumed-token"),
            userId = userId,
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS),
            consumedAt = Instant.now(),
        )
        every { tokenRepository.findByToken(VerificationToken("consumed-token")) } returns consumedToken

        assertFailsWith<VerificationTokenAlreadyConsumedError> {
            verifyUserEmail.verify(VerifyUserEmailCommand("consumed-token"))
        }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `fails when the user account is already active`() {
        every { userRepository.findById(userId) } returns User.from(
            id = userId,
            email = Email.of("user@example.com"),
            hashedPassword = HashedPassword("hashed"),
            role = UserRole.USER,
            status = UserStatus.ACTIVE,
            registeredAt = Instant.now(),
        )

        assertFailsWith<UserAlreadyActiveError> {
            verifyUserEmail.verify(VerifyUserEmailCommand("valid-token"))
        }
        verify(exactly = 0) { userRepository.save(any()) }
    }

}
