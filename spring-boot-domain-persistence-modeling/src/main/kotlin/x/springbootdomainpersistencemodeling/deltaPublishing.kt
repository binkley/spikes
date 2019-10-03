package x.springbootdomainpersistencemodeling

import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher

fun <Resource, Event : ApplicationEvent> notifyIfChanged(
        before: Resource?, after: Resource?,
        publisher: ApplicationEventPublisher,
        event: (Resource?, Resource?) -> Event) {
    if (before == after) return
    publisher.publishEvent(event(before, after))
}
