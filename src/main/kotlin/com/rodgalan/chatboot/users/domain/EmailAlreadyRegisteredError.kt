package com.rodgalan.chatboot.users.domain

class EmailAlreadyRegisteredError(email: String) : RuntimeException("Email already registered: $email")
