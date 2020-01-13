package hm.binkley.scratch

import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.tracing.annotation.NewSpan

@Controller("/foo")
class FooController(
    private val publisher: ApplicationEventPublisher
) {
    @Get
    @NewSpan("foo")
    fun get(): FooJson {
        try {
            return FooJson("Brian", 42)
        } finally {
            publisher.publishEvent(FooAuditEvent(this))
        }
    }
}

data class FooJson(val name: String, val number: Int)
class FooAuditEvent(source: Any) : AuditEvent(source)
