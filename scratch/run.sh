#!/bin/sh

./mvnw "$@" &&
    exec java -jar target/scratch-0-SNAPSHOT-jar-with-dependencies.jar
