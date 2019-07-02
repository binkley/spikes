package x.micronaut

import io.micronaut.http.MediaType.TEXT_PLAIN
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/hello")
class HelloController {
    @Get(produces = [TEXT_PLAIN])
    fun greeting(): String {
        return "Hello World"
    }
}
