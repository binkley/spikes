package x.domainpersistencemodeling

import ch.tutteli.atrium.creating.ReportingAssertionPlant
import ch.tutteli.atrium.verbs.expect
import io.micronaut.context.event.ApplicationEventListener
import javax.inject.Singleton

@Singleton
class TestListener<Event : DomainChangedEvent<*>>
    : ApplicationEventListener<Event> {
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
