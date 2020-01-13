package hm.binkley.scratch

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.tracing.annotation.NewSpan

@Controller("/foo")
open class FooController {
    @Get
    @NewSpan("foo")
    open fun get() = FooJson("Brian", 42)
}

data class FooJson(val name: String, val number: Int)
