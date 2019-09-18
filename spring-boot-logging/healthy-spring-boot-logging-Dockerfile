FROM adoptopenjdk/openjdk11:slim AS java-build

EXPOSE 8080

COPY ./build/libs/spring-boot-logging-0-SNAPSHOT.jar ./
CMD ["java", "-jar", "spring-boot-logging-0-SNAPSHOT.jar", "-c"]
