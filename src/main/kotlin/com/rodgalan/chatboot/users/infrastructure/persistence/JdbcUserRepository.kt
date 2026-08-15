package com.rodgalan.chatboot.users.infrastructure.persistence

import com.rodgalan.chatboot.users.domain.Email
import com.rodgalan.chatboot.users.domain.HashedPassword
import com.rodgalan.chatboot.users.domain.User
import com.rodgalan.chatboot.users.domain.UserId
import com.rodgalan.chatboot.users.domain.UserRepository
import com.rodgalan.chatboot.users.domain.UserRole
import com.rodgalan.chatboot.users.domain.UserStatus
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.UUID

@Repository
class JdbcUserRepository(private val jdbcClient: JdbcClient) : UserRepository {

    override fun findById(id: UserId): User? =
        jdbcClient.sql(
            """
            SELECT id, email, hashed_password, role, status, registered_at
            FROM users
            WHERE id = :id
            """.trimIndent(),
        )
            .param("id", id.value)
            .query { rs, _ -> rs.toUser() }
            .optional()
            .orElse(null)

    override fun findByEmail(email: Email): User? =
        jdbcClient.sql(
            """
            SELECT id, email, hashed_password, role, status, registered_at
            FROM users
            WHERE email = :email
            """.trimIndent(),
        )
            .param("email", email.value)
            .query { rs, _ -> rs.toUser() }
            .optional()
            .orElse(null)

    override fun save(user: User) {
        jdbcClient.sql(
            """
            INSERT INTO users (id, email, hashed_password, role, status, registered_at)
            VALUES (:id, :email, :hashedPassword, :role, :status, :registeredAt)
            ON CONFLICT (id) DO UPDATE SET status = :status
            """.trimIndent(),
        )
            .param("id", user.id.value)
            .param("email", user.email.value)
            .param("hashedPassword", user.hashedPassword.value)
            .param("role", user.role.name)
            .param("status", user.status.name)
            .param("registeredAt", Timestamp.from(user.registeredAt))
            .update()
    }

    private fun ResultSet.toUser(): User = User.from(
        id = UserId(getObject("id", UUID::class.java)),
        email = Email.of(getString("email")),
        hashedPassword = HashedPassword(getString("hashed_password")),
        role = UserRole.valueOf(getString("role")),
        status = UserStatus.valueOf(getString("status")),
        registeredAt = getTimestamp("registered_at").toInstant(),
    )
}
