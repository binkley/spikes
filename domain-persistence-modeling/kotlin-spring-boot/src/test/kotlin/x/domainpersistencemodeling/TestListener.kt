package x.domainpersistencemodeling

import ch.tutteli.atrium.creating.ReportingAssertionPlant
import ch.tutteli.atrium.verbs.expect
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

/** @todo Do not listen for all domain changed events, just [Event] ones */
@Component
class TestListener<Event : DomainChangedEvent<*>>
    : ApplicationListener<Event> {
    private val received = mutableListOf<Event>()

    /** Creates a `List` expectation for Atrium, and resets the listener. */
    val expectNext: ReportingAssertionPlant<List<Event>>
        get() = expect(received.toList()).also {
            reset()
        }

    override fun onApplicationEvent(event: Event) {
        received.add(event)
    }

    fun reset() = received.clear()

    @Suppress("unused")
    fun dump() = println(received)
}
