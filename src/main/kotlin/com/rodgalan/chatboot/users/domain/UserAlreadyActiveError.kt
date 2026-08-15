package com.rodgalan.chatboot.users.domain

class UserAlreadyActiveError(userId: String) : RuntimeException("User already active: $userId")
