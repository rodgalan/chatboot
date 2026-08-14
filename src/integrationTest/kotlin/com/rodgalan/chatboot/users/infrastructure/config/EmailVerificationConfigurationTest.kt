package com.rodgalan.chatboot.users.infrastructure.config

import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mail.javamail.JavaMailSender

@SpringBootTest
class EmailVerificationConfigurationTest {

    @Autowired
    lateinit var mailSender: JavaMailSender

    @Autowired
    lateinit var emailVerificationProperties: EmailVerificationProperties

    @Test
    fun `loads the JavaMailSender bean and binds the email verification properties`() {
        assertNotNull(mailSender)
        assertEquals("no-reply@chatboot.local", emailVerificationProperties.from)
        assertEquals("http://localhost:3000/verify-email?token={token}", emailVerificationProperties.verificationUrlTemplate)
        assertEquals(Duration.ofHours(24), emailVerificationProperties.tokenTtl)
    }

}
