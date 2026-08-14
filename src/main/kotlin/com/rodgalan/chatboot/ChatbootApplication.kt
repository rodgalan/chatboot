package com.rodgalan.chatboot

import com.rodgalan.chatboot.users.infrastructure.config.EmailVerificationProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(EmailVerificationProperties::class)
class ChatbootApplication

fun main(args: Array<String>) {
	runApplication<ChatbootApplication>(*args)
}
