package com.rodgalan.chatboot.users.infrastructure.hashing

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class Pbkdf2PasswordHasherTest {

    private val hasher = Pbkdf2PasswordHasher()

    @Test
    fun `matches a password against its own hash`() {
        val hashed = hasher.hash("Sup3r!Secret123")

        assertTrue(hasher.matches("Sup3r!Secret123", hashed))
    }

    @Test
    fun `does not match a different password`() {
        val hashed = hasher.hash("Sup3r!Secret123")

        assertFalse(hasher.matches("Different!Secret123", hashed))
    }

    @Test
    fun `produces a different hash for the same password on each call`() {
        val first = hasher.hash("Sup3r!Secret123")
        val second = hasher.hash("Sup3r!Secret123")

        assertNotEquals(first, second)
    }

}
