package com.rodgalan.chatboot.users.infrastructure.events

import com.rodgalan.chatboot.users.domain.DomainEventPublisher
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringDomainEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
) : DomainEventPublisher {

    override fun publish(event: Any) {
        applicationEventPublisher.publishEvent(event)
    }
}
