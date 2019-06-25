package hello.world;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import static io.micronaut.http.MediaType.TEXT_PLAIN;

@Controller("/hello")
public class HelloController {
    @Get(produces = TEXT_PLAIN)
    public String index() {
        return "Hello World";
    }
}
