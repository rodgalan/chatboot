package com.rodgalan.chatboot.users.domain

class VerificationTokenExpiredError(token: String) : RuntimeException("Verification token expired: $token")
