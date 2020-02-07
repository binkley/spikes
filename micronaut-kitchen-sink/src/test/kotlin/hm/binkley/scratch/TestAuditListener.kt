package hm.binkley.scratch

import io.micronaut.context.event.ApplicationEventListener
import javax.inject.Singleton

@Singleton
class TestAuditListener(
    private val _events: MutableList<AuditEvent> = mutableListOf()
) : ApplicationEventListener<AuditEvent> {
    val events: List<AuditEvent>
        get() = _events

    override fun onApplicationEvent(event: AuditEvent) {
        _events += event
    }
}
