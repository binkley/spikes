# Spring Boot logging

Various Spring Boot logging niceties

## Run the from command line

First, as a "local" program for developers:

1. Run `./mvnw` or `./gradlew`.
2. Run `java -jar build/libs/spring-boot-logging-0.0.1-SNAPSHOT.jar --help`.

Second, as a "production" project with structured logging (JSON; eg, for ELK):

1. Run `./mvnw` or `./gradlew`.
2. Run `java -jar build/libs/spring-boot-logging-0.0.1-SNAPSHOT.jar --json-logging`.
3. For easier reading, pipe the previous step through `| jq -cC . | less`
   (single line per JSON) or `| jq -C . | less -R` (pretty-printed JSON).

## Features

General:

* Debug logging as it initializes with `-Dlogging.debug=true`
* Early control of logging during program initialization
* UTC timestamps
* Controller and Feign logging with Logbook
* Sleuth tracing through controllers and Feign, including responses
* Problem RFC responses and logging
  - Constraint violations respond with 422 status (no examples)
  - Feign exceptions respond with 502 status (one example)
* Distinct logging from alerting
* Suppress request body logging, and only show response bodies on error
* Top-level custom JSON properties for request/response logging
* Counting SQL queries for performance tests
* Distinguish 502 (server issue) from 503 (remote issue)
* [Prometheus queries](#prometheus-queries)

Local development:

* Full-color Spring Boot logging
* Human-friendly HTTP logging
* Maven and Gradle

Production use:

* JSON logging suitable for Logstash with `--json-logging`
* Embedded JSON payloads (not quoted-string JSON)
* Custom JSON properties (ie, "environment")
* Micrometer timings on Feign clients and Spring Data repositories

## Key files

* [`AssertionsForTracingLogs`](src/test/java/x/loggy/AssertionsForTracingLogs.java)
* [`SqlQueries`](src/main/java/x/loggy/data/SqlQueries.java)
* [`TraceRequestInterceptor`](src/main/java/x/loggy/TraceRequestInterceptor.java)
* [`TraceResponseFilter`](src/main/java/x/loggy/TraceResponseFilter.java)
* [`application.yml`](src/main/resources/application.yml)
* [`bootstrap.yml`](src/main/resources/bootstrap.yml)
* [configuration classes](src/main/java/x/loggy/configuration/)
* [`logback-spring.xml`](src/main/resources/logback-spring.xml)

## Prometheus queries

```sh
$ ./gradlew bootJar
$ ./docker-compose build
$ ./docker-compose up
$ open http://localhost:9090
```
