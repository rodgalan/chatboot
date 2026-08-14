package com.rodgalan.chatboot.users.infrastructure.hashing

import com.rodgalan.chatboot.users.domain.HashedPassword
import com.rodgalan.chatboot.users.domain.PasswordHasher
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

@Component
class Pbkdf2PasswordHasher : PasswordHasher {

    override fun hash(rawPassword: String): HashedPassword {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val digest = deriveKey(rawPassword, salt, ITERATIONS)
        return HashedPassword("$ITERATIONS:${encode(salt)}:${encode(digest)}")
    }

    override fun matches(rawPassword: String, hashedPassword: HashedPassword): Boolean {
        val (iterations, salt, expectedDigest) = decode(hashedPassword)
        val actualDigest = deriveKey(rawPassword, salt, iterations)
        return actualDigest.contentEquals(expectedDigest)
    }

    private fun deriveKey(rawPassword: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(rawPassword.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
    }

    private fun decode(hashedPassword: HashedPassword): Triple<Int, ByteArray, ByteArray> {
        val (iterations, salt, digest) = hashedPassword.value.split(":")
        return Triple(iterations.toInt(), Base64.getDecoder().decode(salt), Base64.getDecoder().decode(digest))
    }

    private fun encode(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 210_000
        private const val KEY_LENGTH_BITS = 256
        private const val SALT_LENGTH_BYTES = 16
    }
}
