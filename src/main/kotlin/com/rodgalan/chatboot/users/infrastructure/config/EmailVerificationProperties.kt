package com.rodgalan.chatboot.users.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "chatboot.users.email-verification")
data class EmailVerificationProperties(
	val from: String,
	val verificationUrlTemplate: String,
	val tokenTtl: Duration,
)
