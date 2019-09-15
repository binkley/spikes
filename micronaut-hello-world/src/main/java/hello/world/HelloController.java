package hello.world;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;

import javax.validation.Valid;

import static io.micronaut.http.MediaType.TEXT_PLAIN;

@Controller("/hello")
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
     * @param roundTripData Garbage in
     *
     * @return Garbage out
     */
    @Post
    public RoundTripData roundTrip(final @Valid RoundTripData roundTripData) {
        return roundTripData;
    }
}
