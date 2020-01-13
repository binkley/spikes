package hm.binkley.scratch

import io.micronaut.context.event.ApplicationEvent

open class AuditEvent(source: Any) : ApplicationEvent(source)
