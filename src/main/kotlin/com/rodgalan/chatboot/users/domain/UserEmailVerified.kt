package com.rodgalan.chatboot.users.domain

import java.time.Instant

data class UserEmailVerified(
    val userId: String,
    val occurredOn: Instant,
)
