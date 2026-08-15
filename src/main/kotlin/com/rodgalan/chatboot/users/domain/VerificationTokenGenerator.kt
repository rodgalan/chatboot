package com.rodgalan.chatboot.users.domain

interface VerificationTokenGenerator {
    fun generate(): VerificationToken
}
