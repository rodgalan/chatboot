package com.rodgalan.chatboot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ChatbootApplication

fun main(args: Array<String>) {
	runApplication<ChatbootApplication>(*args)
}
