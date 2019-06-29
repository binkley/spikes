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
    /**
     * Announces ourselves.
     *
     * @return A greeting
     */
    @Get(produces = TEXT_PLAIN)
    public String index() {
        return "Hello World";
    }

    /**
     * Makes a roundtrip.
     *
     * @param roundtripData Garbage in
     *
     * @return Garbage out
     */
    @Post
    public RoundtripData roundtrip(final @Valid RoundtripData roundtripData) {
        return roundtripData;
    }
}
