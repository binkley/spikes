# Micronaut Kitchen Sink

All the things

Try `./gradlew clean build`.

## Stack

- Kotlin, Kotlintest, Mockk
- Micronaut, Jackson, Logback
- Swagger (no UI client)
- Jaeger (**TODO**: Zipkin)
- Prometheus

## Build

- Gradle with Kotlin (`settings.gradle.kts`, `build.gradle.kts`)
- Static code analysis (detekt)
- Format linting with automatic fixes (ktlint)
- Full code coverage (jacoco)
- Flaky test detection (**IN PROGRESS**)

## Deployment

### TODO

- Pipeline

## Alerting

### TODO

## Auditing

- Leverage event publishing

### TODO

- Publish to business analytics

## Logging

- JSON logging by default
- Logback configuration via code rather than XML

### TODO

- Local dev gets pretty printed text rather than JSON

  Teach `LogbackConfiguration` about environments.
- Publish to aggregation

## Management

- Git build details in `/admin/info`

### TODO

- Dashboard

## Tracing

### TODO

- Use Zipkin rather tha Jaeger

  Simple swap out is not producing HTTP trace headers for Zipkin.
- Publish to aggregation
