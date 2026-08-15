package com.rodgalan.chatboot.users.domain

import java.time.Duration
import java.time.Instant

class EmailVerificationToken private constructor(
    val token: VerificationToken,
    val userId: UserId,
    val expiresAt: Instant,
    val consumedAt: Instant?,
) {
    fun isExpiredAt(instant: Instant): Boolean = instant.isAfter(expiresAt)

    fun isConsumed(): Boolean = consumedAt != null

    fun consume(): EmailVerificationToken = EmailVerificationToken(token, userId, expiresAt, Instant.now())

    companion object {
        fun issue(token: VerificationToken, userId: UserId, ttl: Duration): EmailVerificationToken =
            EmailVerificationToken(token, userId, Instant.now().plus(ttl), null)

        fun from(token: VerificationToken, userId: UserId, expiresAt: Instant, consumedAt: Instant?): EmailVerificationToken =
            EmailVerificationToken(token, userId, expiresAt, consumedAt)
    }
}
