package com.rodgalan.chatboot.users.domain

interface PasswordHasher {
    fun hash(rawPassword: String): HashedPassword
    fun matches(rawPassword: String, hashedPassword: HashedPassword): Boolean
}
