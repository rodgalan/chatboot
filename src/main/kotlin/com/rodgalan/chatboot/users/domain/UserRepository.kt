package com.rodgalan.chatboot.users.domain

interface UserRepository {
    fun findById(id: UserId): User?
    fun findByEmail(email: Email): User?
    fun save(user: User)
}
