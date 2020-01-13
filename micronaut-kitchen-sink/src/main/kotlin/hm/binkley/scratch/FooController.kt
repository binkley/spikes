package hm.binkley.scratch

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/foo")
class FooController {
    @Get
    fun get() = FooJson("Brian", 42)
}

data class FooJson(val name: String, val number: Int)
