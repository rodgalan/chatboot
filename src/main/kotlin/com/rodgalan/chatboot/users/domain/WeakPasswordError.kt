package com.rodgalan.chatboot.users.domain

class WeakPasswordError : RuntimeException("Password does not meet the security policy")
