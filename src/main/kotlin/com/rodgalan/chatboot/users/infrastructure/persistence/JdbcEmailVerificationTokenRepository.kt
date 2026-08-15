package com.rodgalan.chatboot.users.infrastructure.persistence

import com.rodgalan.chatboot.users.domain.EmailVerificationToken
import com.rodgalan.chatboot.users.domain.EmailVerificationTokenRepository
import com.rodgalan.chatboot.users.domain.UserId
import com.rodgalan.chatboot.users.domain.VerificationToken
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.UUID

@Repository
class JdbcEmailVerificationTokenRepository(private val jdbcClient: JdbcClient) : EmailVerificationTokenRepository {

    override fun findByToken(token: VerificationToken): EmailVerificationToken? =
        jdbcClient.sql(
            """
            SELECT token, user_id, expires_at, consumed_at
            FROM email_verification_tokens
            WHERE token = :token
            """.trimIndent(),
        )
            .param("token", token.value)
            .query { rs, _ -> rs.toEmailVerificationToken() }
            .optional()
            .orElse(null)

    override fun save(token: EmailVerificationToken) {
        jdbcClient.sql(
            """
            INSERT INTO email_verification_tokens (token, user_id, expires_at, consumed_at)
            VALUES (:token, :userId, :expiresAt, :consumedAt)
            ON CONFLICT (token) DO UPDATE SET consumed_at = :consumedAt
            """.trimIndent(),
        )
            .param("token", token.token.value)
            .param("userId", token.userId.value)
            .param("expiresAt", Timestamp.from(token.expiresAt))
            .param("consumedAt", token.consumedAt?.let { Timestamp.from(it) })
            .update()
    }

    private fun ResultSet.toEmailVerificationToken(): EmailVerificationToken = EmailVerificationToken.from(
        token = VerificationToken(getString("token")),
        userId = UserId(getObject("user_id", UUID::class.java)),
        expiresAt = getTimestamp("expires_at").toInstant(),
        consumedAt = getTimestamp("consumed_at")?.toInstant(),
    )
}
