package x.retryable

import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.micronaut.retry.annotation.Retryable
import x.retryable.RetryingClient.Companion.attempts
import x.retryable.RetryingClient.Companion.delay

interface Retrying {
    @Post
    fun retryMe(): String
}

@Client("https://no-where/retry-me")
@Retryable(attempts = attempts, delay = delay) // default is 3 times
interface RetryingClient : Retrying {
    override fun retryMe(): String

    companion object {
        const val attempts = "2"
        const val delay = "1ms"
    }
}
