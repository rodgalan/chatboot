package com.rodgalan.chatboot.users.domain

class InvalidEmailFormatError(email: String) : RuntimeException("Invalid email format: $email")
