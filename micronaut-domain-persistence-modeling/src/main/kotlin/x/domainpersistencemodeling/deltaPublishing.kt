package x.domainpersistencemodeling

import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher

fun <Resource, Event : ApplicationEvent> notifyIfChanged(
        before: Resource?, after: Resource?,
        publisher: ApplicationEventPublisher,
        event: (Resource?, Resource?) -> Event) {
    if (before == after) return
    publisher.publishEvent(event(before, after))
}
