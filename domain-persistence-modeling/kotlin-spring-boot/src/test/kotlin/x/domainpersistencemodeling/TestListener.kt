package x.domainpersistencemodeling

import ch.tutteli.atrium.creating.ReportingAssertionPlant
import ch.tutteli.atrium.verbs.expect
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class TestListener<Event : DomainChangedEvent<*>>
    : ApplicationListener<Event> {
    private val received = mutableListOf<Event>()

    fun reset() {
        received.clear()
    }

    override fun onApplicationEvent(event: Event) {
        received.add(event)
    }

    /** Creates a `List` expectation for AssertJ, and resets the listener. */
    val expectNext: ReportingAssertionPlant<List<Event>>
        get() = expect(received.toList()).also {
            reset()
        }
}
