package x.domainpersistencemodeling

import org.springframework.context.ApplicationEvent

abstract class DomainChangedEvent<Resource>(
        before: Resource?,
        after: Resource?)
    : ApplicationEvent(after ?: before!!)
