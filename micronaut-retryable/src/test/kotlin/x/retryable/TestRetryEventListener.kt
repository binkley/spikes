package x.retryable

import io.micronaut.retry.event.RetryEvent
import io.micronaut.runtime.event.annotation.EventListener
import javax.inject.Singleton

@Singleton
class TestRetryEventListener {
    private val _events = mutableListOf<RetryEvent>()
    val events: List<RetryEvent>
        get() = _events

    fun reset() = _events.clear()

    @EventListener
    internal fun onRetryEvent(event: RetryEvent) {
        _events += event
    }
}
