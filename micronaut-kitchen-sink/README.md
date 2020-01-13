# Micronaut Kitchen Sink

All the things

Try `./gradlew clean build`.

## Stack

- Kotlin, Kotlintest, Mockk
- Micronaut, Jackson, Logback
- Swagger
- Jaeger (**TODO**: Zipkin)
- Prometheus

## Build

- Gradle with Kotlin
- Format linting with automatic fixes
- Full code coverage

## Auditing

- Leverage event publishing

## Logging

- JSON logging by default
- Logback configuration via code rather than XML

### TODO

- Local dev gets pretty printed text rather than JSON

  Teach `LogbackConfiguration` about environments.

## Management

- Git build details in `/admin/info`

## Tracing

### TODO

- Use Zipkin rather tha Jaeger

  Simple swap out is not producing HTTP trace headers for Zipkin.
