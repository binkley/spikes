#!/usr/bin/env bash

jar=target/scratch-0-SNAPSHOT-jar-with-dependencies.jar

test -r $jar || ./mvnw -C package

case $# in
0) class="-jar $jar" ;;
1) class="-cp $jar x.scratch.${1^}Kt" ;;
*)
    echo "Usage: $0 [FILE-NAME]" >&2
    exit 2
    ;;
esac

exec java $class
