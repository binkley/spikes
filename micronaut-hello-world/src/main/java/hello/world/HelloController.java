package hello.world;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.validation.Validated;

import javax.validation.Valid;

import static io.micronaut.http.MediaType.TEXT_PLAIN;

@Controller("/hello")
@Validated
public class HelloController {
    @Get(produces = TEXT_PLAIN)
    public String index() {
        return "Hello World";
    }

    @Post
    public SampleData roundtrip(final @Valid SampleData sampleData) {
        return sampleData;
    }
}
