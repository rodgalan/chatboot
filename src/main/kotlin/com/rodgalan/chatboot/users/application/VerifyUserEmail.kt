package com.rodgalan.chatboot.users.application

import com.rodgalan.chatboot.users.domain.DomainEventPublisher
import com.rodgalan.chatboot.users.domain.EmailVerificationTokenRepository
import com.rodgalan.chatboot.users.domain.UserEmailVerified
import com.rodgalan.chatboot.users.domain.UserRepository
import com.rodgalan.chatboot.users.domain.VerificationToken
import com.rodgalan.chatboot.users.domain.VerificationTokenAlreadyConsumedError
import com.rodgalan.chatboot.users.domain.VerificationTokenExpiredError
import com.rodgalan.chatboot.users.domain.VerificationTokenNotFoundError
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class VerifyUserEmail(
    private val tokenRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    private val domainEventPublisher: DomainEventPublisher,
) {
    fun verify(command: VerifyUserEmailCommand) {
        val token = VerificationToken(command.token)
        val verificationToken = tokenRepository.findByToken(token)
            ?: throw VerificationTokenNotFoundError(command.token)

        if (verificationToken.isConsumed()) {
            throw VerificationTokenAlreadyConsumedError(command.token)
        }

        val now = Instant.now()
        if (verificationToken.isExpiredAt(now)) {
            throw VerificationTokenExpiredError(command.token)
        }

        val user = requireNotNull(userRepository.findById(verificationToken.userId))

        val activatedUser = user.activate()
        userRepository.save(activatedUser)
        tokenRepository.save(verificationToken.consume())

        domainEventPublisher.publish(
            UserEmailVerified(userId = activatedUser.id.value.toString(), occurredOn = now),
        )
    }
}
