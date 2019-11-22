package x.domainpersistencemodeling

import io.micronaut.context.event.ApplicationEvent

abstract class DomainChangedEvent<Snapshot>(
        before: Snapshot?,
        after: Snapshot?)
    : ApplicationEvent(after ?: before!!)
