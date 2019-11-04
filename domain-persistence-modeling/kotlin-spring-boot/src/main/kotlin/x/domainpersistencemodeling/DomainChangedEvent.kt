package x.domainpersistencemodeling

import org.springframework.context.ApplicationEvent

abstract class DomainChangedEvent<Snapshot>(
        before: Snapshot?,
        after: Snapshot?)
    : ApplicationEvent(after ?: before!!)
