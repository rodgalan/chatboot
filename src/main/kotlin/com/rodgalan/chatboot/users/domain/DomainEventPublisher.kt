package com.rodgalan.chatboot.users.domain

interface DomainEventPublisher {
    fun publish(event: Any)
}
