package com.rodgalan.chatboot.users.infrastructure.token

import com.rodgalan.chatboot.users.domain.VerificationToken
import com.rodgalan.chatboot.users.domain.VerificationTokenGenerator
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64

@Component
class SecureRandomVerificationTokenGenerator : VerificationTokenGenerator {

    override fun generate(): VerificationToken {
        val bytes = ByteArray(TOKEN_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        return VerificationToken(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes))
    }

    companion object {
        private const val TOKEN_LENGTH_BYTES = 32
    }
}
