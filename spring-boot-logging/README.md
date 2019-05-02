# Spring Boot logging

Various Spring Boot logging niceties

## Run the from command line

First, as a "local" program for developers:

1. Run `./gradlew build`.
2. Run `java -jar build/libs/spring-boot-logging-0.0.1-SNAPSHOT.jar`.

Second, as a "production" project with JSON log ingestion (ie, ELK):

1. Run `./gradlew build`.
2. Run `java -Dspring.profiles.active=json -jar build/libs/spring-boot-logging-0.0.1-SNAPSHOT.jar`.
3. For easier reading, pipe the previous step through `| jq -C . | less`.

## Features

General:

* Early control of logging during program initialization
* UTC timestamps
* Sleuth tracing
* Controller and Feign logging with Logbook

Local development:

* Full-color Spring Boot logging
* Human-friendly HTTP logging

Production use:

* JSON logging suitable for Logstash
* Embedded JSON payloads (not quoted-string JSON)
* Custom JSON properties (ie, "environment")
