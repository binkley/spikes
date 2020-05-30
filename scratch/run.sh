#!/bin/sh

jar=target/scratch-0-SNAPSHOT-jar-with-dependencies.jar

test -r $jar || ./mvnw -C package

exec java -jar $jar
