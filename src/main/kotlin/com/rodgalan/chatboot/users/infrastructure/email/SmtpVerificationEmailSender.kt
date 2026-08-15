package com.rodgalan.chatboot.users.infrastructure.email

import com.rodgalan.chatboot.users.domain.Email
import com.rodgalan.chatboot.users.domain.VerificationEmailSender
import com.rodgalan.chatboot.users.domain.VerificationToken
import com.rodgalan.chatboot.users.infrastructure.config.EmailVerificationProperties
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
class SmtpVerificationEmailSender(
    private val mailSender: JavaMailSender,
    private val properties: EmailVerificationProperties,
) : VerificationEmailSender {

    override fun send(email: Email, token: VerificationToken) {
        val verificationUrl = properties.verificationUrlTemplate.replace("{token}", token.value)

        val message = SimpleMailMessage().apply {
            setFrom(properties.from)
            setTo(email.value)
            setSubject(SUBJECT)
            setText(body(verificationUrl))
        }

        mailSender.send(message)
    }

    private fun body(verificationUrl: String) = """
        Welcome to Chatboot!

        Confirm your email address to activate your account:
        $verificationUrl

        This link expires in 24 hours.
        If you did not create this account, you can ignore this email.
    """.trimIndent()

    companion object {
        private const val SUBJECT = "Confirm your email address"
    }
}
