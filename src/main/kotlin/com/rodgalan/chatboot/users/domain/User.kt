package com.rodgalan.chatboot.users.domain

import java.time.Instant

class User private constructor(
    val id: UserId,
    val email: Email,
    val hashedPassword: HashedPassword,
    val role: UserRole,
    val status: UserStatus,
    val registeredAt: Instant,
) {
    fun activate(): User {
        if (status == UserStatus.ACTIVE) {
            throw UserAlreadyActiveError(id.value.toString())
        }
        return User(id, email, hashedPassword, role, UserStatus.ACTIVE, registeredAt)
    }

    companion object {
        fun register(email: Email, hashedPassword: HashedPassword): User =
            User(
                id = UserId.generate(),
                email = email,
                hashedPassword = hashedPassword,
                role = UserRole.USER,
                status = UserStatus.NON_VALIDATED,
                registeredAt = Instant.now(),
            )

        fun from(
            id: UserId,
            email: Email,
            hashedPassword: HashedPassword,
            role: UserRole,
            status: UserStatus,
            registeredAt: Instant,
        ): User = User(id, email, hashedPassword, role, status, registeredAt)
    }
}
