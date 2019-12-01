package x.domainpersistencemodeling

import io.micronaut.context.event.ApplicationEventListener
import javax.inject.Singleton

@Singleton
class ProgrammableListener<Event : DomainChangedEvent<*>>
    : ApplicationEventListener<Event> {
    var fail: Boolean = false

    override fun onApplicationEvent(event: Event) {
        if (fail) throw DomainException(
            "Failed on purpose"
        )
    }
}
