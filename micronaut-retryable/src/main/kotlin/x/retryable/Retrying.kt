package x.retryable

import io.micronaut.retry.annotation.Retryable

open class Retrying {
    var retried = 0

    @Retryable // default is 3 times
    open fun retryMe() {
        ++retried
        throw IllegalStateException("I fail; please retry me")
    }
}
