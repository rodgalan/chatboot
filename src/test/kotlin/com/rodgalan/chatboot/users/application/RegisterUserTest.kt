package com.rodgalan.chatboot.users.application

import com.rodgalan.chatboot.users.domain.DomainEventPublisher
import com.rodgalan.chatboot.users.domain.Email
import com.rodgalan.chatboot.users.domain.EmailAlreadyRegisteredError
import com.rodgalan.chatboot.users.domain.HashedPassword
import com.rodgalan.chatboot.users.domain.InvalidEmailFormatError
import com.rodgalan.chatboot.users.domain.PasswordHasher
import com.rodgalan.chatboot.users.domain.User
import com.rodgalan.chatboot.users.domain.UserRegistered
import com.rodgalan.chatboot.users.domain.UserRepository
import com.rodgalan.chatboot.users.domain.UserRole
import com.rodgalan.chatboot.users.domain.UserStatus
import com.rodgalan.chatboot.users.domain.WeakPasswordError
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RegisterUserTest {

    private val userRepository = mockk<UserRepository>()
    private val passwordHasher = mockk<PasswordHasher>()
    private val domainEventPublisher = mockk<DomainEventPublisher>()
    private lateinit var registerUser: RegisterUser

    private val validPassword = "Sup3r!Secret123"

    @BeforeTest
    fun setUp() {
        registerUser = RegisterUser(userRepository, passwordHasher, domainEventPublisher)
        every { userRepository.findByEmail(any()) } returns null
        every { passwordHasher.hash(any()) } returns HashedPassword("hashed")
        every { userRepository.save(any()) } returns Unit
        every { domainEventPublisher.publish(any()) } returns Unit
    }

    @Test
    fun `registers a user in non validated state with the user role`() {
        val userSlot = slot<User>()
        every { userRepository.save(capture(userSlot)) } returns Unit

        registerUser.register(RegisterUserCommand("user@example.com", validPassword))

        assertEquals(UserStatus.NON_VALIDATED, userSlot.captured.status)
        assertEquals(UserRole.USER, userSlot.captured.role)
        assertEquals(Email.of("user@example.com"), userSlot.captured.email)
    }

    @Test
    fun `stores the password hashed`() {
        every { passwordHasher.hash(validPassword) } returns HashedPassword("hashed-value")
        val userSlot = slot<User>()
        every { userRepository.save(capture(userSlot)) } returns Unit

        registerUser.register(RegisterUserCommand("user@example.com", validPassword))

        assertEquals(HashedPassword("hashed-value"), userSlot.captured.hashedPassword)
    }

    @Test
    fun `publishes a user registered domain event`() {
        val eventSlot = slot<UserRegistered>()
        every { domainEventPublisher.publish(capture(eventSlot)) } returns Unit

        registerUser.register(RegisterUserCommand("user@example.com", validPassword))

        assertEquals("user@example.com", eventSlot.captured.email)
    }

    @Test
    fun `fails when the email is already registered`() {
        every { userRepository.findByEmail(Email.of("user@example.com")) } returns
            User.register(Email.of("user@example.com"), HashedPassword("hashed"))

        assertFailsWith<EmailAlreadyRegisteredError> {
            registerUser.register(RegisterUserCommand("user@example.com", validPassword))
        }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `fails when the email format is invalid`() {
        assertFailsWith<InvalidEmailFormatError> {
            registerUser.register(RegisterUserCommand("not-an-email", validPassword))
        }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `fails when the password is shorter than the minimum length`() {
        assertFailsWith<WeakPasswordError> {
            registerUser.register(RegisterUserCommand("user@example.com", "Sh0rt!"))
        }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `fails when the password misses an uppercase letter`() {
        assertFailsWith<WeakPasswordError> {
            registerUser.register(RegisterUserCommand("user@example.com", "sup3r!secret123"))
        }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `fails when the password misses a lowercase letter`() {
        assertFailsWith<WeakPasswordError> {
            registerUser.register(RegisterUserCommand("user@example.com", "SUP3R!SECRET123"))
        }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `fails when the password misses a digit`() {
        assertFailsWith<WeakPasswordError> {
            registerUser.register(RegisterUserCommand("user@example.com", "Super!Secretxxx"))
        }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `fails when the password misses a special character`() {
        assertFailsWith<WeakPasswordError> {
            registerUser.register(RegisterUserCommand("user@example.com", "Sup3rSecret123"))
        }
        verify(exactly = 0) { userRepository.save(any()) }
    }

}
