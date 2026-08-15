package com.rodgalan.chatboot.users.application

import com.rodgalan.chatboot.users.domain.Email
import com.rodgalan.chatboot.users.domain.EmailVerificationToken
import com.rodgalan.chatboot.users.domain.EmailVerificationTokenRepository
import com.rodgalan.chatboot.users.domain.UserId
import com.rodgalan.chatboot.users.domain.UserRegistered
import com.rodgalan.chatboot.users.domain.VerificationEmailSender
import com.rodgalan.chatboot.users.domain.VerificationTokenGenerator
import com.rodgalan.chatboot.users.infrastructure.config.EmailVerificationProperties
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SendVerificationEmailOnUserRegistered(
    private val tokenGenerator: VerificationTokenGenerator,
    private val tokenRepository: EmailVerificationTokenRepository,
    private val emailSender: VerificationEmailSender,
    private val properties: EmailVerificationProperties,
) {
    @EventListener
    fun on(event: UserRegistered) {
        val token = tokenGenerator.generate()
        val verificationToken = EmailVerificationToken.issue(
            token = token,
            userId = UserId(UUID.fromString(event.userId)),
            ttl = properties.tokenTtl,
        )
        tokenRepository.save(verificationToken)

        emailSender.send(Email.of(event.email), token)
    }
}
