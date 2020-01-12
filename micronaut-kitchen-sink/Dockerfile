FROM adoptopenjdk/openjdk11-openj9:jdk-11.0.5_10_openj9-0.17.0-alpine-slim
COPY build/libs/micronaut-kitchen-sink-*-all.jar micronaut-kitchen-sink.jar
EXPOSE 8080
CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar micronaut-kitchen-sink.jar
