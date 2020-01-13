# Micronaut Kitchen Sink

All the things

## Auditing

- Leverage event publishing

## Logging

- JSON logging by default
- Logback configuration via code rather than XML

### TODO

- Local dev gets pretty printed text rather than JSON

  Teach `LogbackConfiguration` about environments.

## Tracing

### TODO

- Use Zipkin rather tha Jaeger

  Simple swap out is not producing HTTP trace headers for Zipkin.
