# Spring Boot logging

Various Spring Boot logging niceties

## Run from command line

First, as a "local" program for developers:

1. Run `./gradlew build`.
2. Run `java -jar build/libs/spring-boot-logging-0.0.1-SNAPSHOT.jar`.

Second, as a "production" project with JSON log ingestion (ie, ELK):

1. Run `./gradlew build`.
2. Run `java -Dspring.profiles.active=json -jar build/libs/spring-boot-logging-0.0.1-SNAPSHOT.jar`.
3. For easier reading, pipe the previous step through `| jq -C . | less`.
