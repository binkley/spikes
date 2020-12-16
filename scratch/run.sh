#!/usr/bin/env bash

jar=target/scratch-0-SNAPSHOT-jar-with-dependencies.jar

test -r $jar || ./mvnw -C package

function mangle-classname() {
    local IFS=.

    local -a parts
    read -r -a parts <<<"$1"
    local last="${parts[-1]}"

    case "$last" in
    *-*|*Kt) ;;
    *) last="${last}Kt" ;;
    esac
    last="${last//-/_}"
    last=""${last^}

    parts[-1]="$last"

    echo "${parts[*]}"
}

case $# in
0) class="-jar $jar" ;;
1) class="-cp $jar x.scratch.$(mangle-classname "$1")" ;;
*)
    echo "Usage: $0 [FILE-NAME]" >&2
    exit 2
    ;;
esac

exec java $class
