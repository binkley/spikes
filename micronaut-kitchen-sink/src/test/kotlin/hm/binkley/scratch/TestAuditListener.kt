package hm.binkley.scratch

import io.micronaut.context.event.ApplicationEventListener
import javax.inject.Singleton

@Singleton
class TestAuditListener(
    private val _events: MutableList<AuditEvent> = mutableListOf()
) : ApplicationEventListener<AuditEvent> {
    /** Copies the current events, and resets the listener. */
    val events: List<AuditEvent>
        get() {
            val events = _events.toList()
            _events.clear()
            return events
        }

    override fun onApplicationEvent(event: AuditEvent) {
        _events += event
    }
}
