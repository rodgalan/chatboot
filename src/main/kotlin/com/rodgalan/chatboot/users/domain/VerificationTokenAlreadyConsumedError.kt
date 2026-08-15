package com.rodgalan.chatboot.users.domain

class VerificationTokenAlreadyConsumedError(token: String) : RuntimeException("Verification token already consumed: $token")
