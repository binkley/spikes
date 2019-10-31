package x.domainpersistencemodeling

import io.micronaut.context.event.ApplicationEvent

abstract class DomainChangedEvent<Resource>(
        before: Resource?,
        after: Resource?)
    : ApplicationEvent(after ?: before!!)
