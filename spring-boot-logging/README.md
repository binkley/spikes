# Spring Boot logging

Various Spring Boot logging niceties

## Run the from command line

First, as a "local" program for developers:

1. Run `./gradlew build`.
2. Run `java -jar build/libs/spring-boot-logging-0.0.1-SNAPSHOT.jar`.

Second, as a "production" project with structured logging (JSON; eg, for ELK):

1. Run `./gradlew build`.
2. Run `java -Dspring.profiles.active=json -jar build/libs/spring-boot-logging-0.0.1-SNAPSHOT.jar`.
3. For easier reading, pipe the previous step through `| jq -cC . | less`
   (single line per JSON) or `| jq -C . less` (pretty-printed JSON).

## Features

General:

* Debug logging as it initializes with `-Dlogging.debug=true`
* Early control of logging during program initialization
* UTC timestamps
* Controller and Feign logging with Logbook
* Sleuth tracing through controllers and Feign, including responses

Local development:

* Full-color Spring Boot logging
* Human-friendly HTTP logging

Production use:

* JSON logging suitable for Logstash with `-Dspring.profiles.active=json`
* Embedded JSON payloads (not quoted-string JSON)
* Custom JSON properties (ie, "environment")

## Key files

* [`AssertionsForTracingLogs`](src/test/java/x/loggy/AssertionsForTracingLogs.java)
* [`TraceLiveTest`](src/test/java/x/loggy/TraceLiveTest.java)
* [`TraceRequestInterceptor`](src/main/java/x/loggy/TraceRequestInterceptor.java)
* [`TraceResponseFilter`](src/main/java/x/loggy/TraceResponseFilter.java)
* [`application.yml`](src/main/resources/application.yml)
* [`bootstrap.yml`](src/main/resources/bootstrap.yml)
* [`logback-spring.xml`](src/main/resources/logback-spring.xml)
