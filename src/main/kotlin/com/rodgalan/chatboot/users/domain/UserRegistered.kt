package com.rodgalan.chatboot.users.domain

import java.time.Instant

data class UserRegistered(
    val userId: String,
    val email: String,
    val occurredOn: Instant,
)
