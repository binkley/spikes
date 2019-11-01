package x.domainpersistencemodeling

import org.springframework.context.event.EventListener
import org.springframework.data.rest.core.event.AfterSaveEvent
import org.springframework.data.rest.core.event.BeforeSaveEvent
import org.springframework.stereotype.Component

@Component
class SaveListener {
    @EventListener(BeforeSaveEvent::class)
    fun onApplicationEvent(event: BeforeSaveEvent) {
        println(event)
    }

    @EventListener(AfterSaveEvent::class)
    fun onApplicationEvent(event: AfterSaveEvent) {
        println(event)
    }
}
