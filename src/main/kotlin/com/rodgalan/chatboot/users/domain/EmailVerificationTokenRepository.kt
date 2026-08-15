package com.rodgalan.chatboot.users.domain

interface EmailVerificationTokenRepository {
    fun findByToken(token: VerificationToken): EmailVerificationToken?
    fun save(token: EmailVerificationToken)
}
