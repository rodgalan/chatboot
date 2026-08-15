package com.rodgalan.chatboot.users.domain

class VerificationTokenNotFoundError(token: String) : RuntimeException("Verification token not found: $token")
