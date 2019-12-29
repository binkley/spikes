#!/bin/sh

./mvnw clean verify \
    && java -jar target/blockchain-0-SNAPSHOT-jar-with-dependencies.jar
