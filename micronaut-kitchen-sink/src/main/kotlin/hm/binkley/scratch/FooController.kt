package hm.binkley.scratch

import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.tracing.annotation.NewSpan
import io.micronaut.validation.Validated

@Controller("/foo")
@Validated
class FooController(
    private val publisher: ApplicationEventPublisher
) {
    @Get
    @NewSpan("foo")
    fun get(): FooJson {
        @Suppress("MagicNumber")
        val response = FooJson("Brian", 42)
        publisher.publishEvent(FooAuditEvent(this))
        return response
    }
}

data class FooJson(val name: String, val number: Int)
class FooAuditEvent(source: Any) : AuditEvent(source)
