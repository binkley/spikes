package x.domainpersistencemodeling

import ch.tutteli.atrium.creating.ReportingAssertionPlant
import ch.tutteli.atrium.verbs.expect
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class TestListener<E : ApplicationEvent> : ApplicationListener<E> {
    private val received = mutableListOf<E>()

    fun reset() {
        received.clear()
    }

    override fun onApplicationEvent(event: E) {
        received.add(event)
    }

    val expectNext: ReportingAssertionPlant<List<E>>
        get() = expect(received.toList()).also {
            reset()
        }
}
