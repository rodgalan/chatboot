package com.rodgalan.chatboot.users.application

import com.rodgalan.chatboot.users.domain.DomainEventPublisher
import com.rodgalan.chatboot.users.domain.Email
import com.rodgalan.chatboot.users.domain.EmailAlreadyRegisteredError
import com.rodgalan.chatboot.users.domain.PasswordHasher
import com.rodgalan.chatboot.users.domain.PasswordPolicy
import com.rodgalan.chatboot.users.domain.User
import com.rodgalan.chatboot.users.domain.UserRegistered
import com.rodgalan.chatboot.users.domain.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RegisterUser(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val domainEventPublisher: DomainEventPublisher,
) {
    fun register(command: RegisterUserCommand) {
        val email = Email.of(command.email)

        if (userRepository.findByEmail(email) != null) {
            throw EmailAlreadyRegisteredError(command.email)
        }

        PasswordPolicy.validate(command.password)

        val hashedPassword = passwordHasher.hash(command.password)
        val user = User.register(email, hashedPassword)
        userRepository.save(user)

        domainEventPublisher.publish(
            UserRegistered(userId = user.id.value.toString(), email = email.value, occurredOn = Instant.now()),
        )
    }
}
