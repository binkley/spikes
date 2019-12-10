package x.retryable

import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.micronaut.retry.annotation.Retryable

interface Retrying {
    @Post
    fun retryMe(): String
}

@Client("https://no-where/retry-me")
@Retryable // default is 3 times
interface RetryingClient : Retrying {
    override fun retryMe(): String
}
