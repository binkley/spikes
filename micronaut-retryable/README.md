# Testing Kotlin Micronaut retryable

A tiny project demonstrating use of logging to test non-functional
requirements (NFRs), to wit, checking a correct retry policy in Micronaut.

For the main trick, see
[`TestRetryAppender`](src/test/kotlin/x/retryable/TestRetryAppender.kt).
