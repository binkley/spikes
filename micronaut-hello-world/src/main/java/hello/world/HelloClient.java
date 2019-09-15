package hello.world;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.retry.annotation.Retryable;
import io.reactivex.Single;

@Client("/hello")
@Retryable(
        attempts = "${hello.retry.attempts:3}",
        delay = "${hello.retry.delay:1s}")
public interface HelloClient {
    @Get
    Single<String> hello();

    @Post
    Single<RoundTripData> roundtrip(final RoundTripData roundtripData);

    @Post("/not-there")
    Single<Void> notThere();
}
