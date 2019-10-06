package x.domainpersistencemodeling

import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher

fun <Resource, Event : DomainChangedEvent<Resource>> notifyIfChanged(
        before: Resource?, after: Resource?,
        publisher: ApplicationEventPublisher,
        event: (Resource?, Resource?) -> Event) {
    if (before == after) return
    publisher.publishEvent(event(before, after))
}

abstract class DomainChangedEvent<Resource>(
        before: Resource?,
        after: Resource?)
    : ApplicationEvent(after ?: before!!)
