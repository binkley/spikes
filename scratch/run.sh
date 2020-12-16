#!/usr/bin/env bash

package=x.scratch
jar=target/scratch-0-SNAPSHOT-jar-with-dependencies.jar

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

function rebuild-if-needed() {
    [[ ! -e "$jar" || -n "$(find src -type f -newer "$jar")" ]]
}

case $# in
0) class="-jar $jar" ;;
1) class="-cp $jar $package.$(mangle-classname "$1")" ;;
*)
    echo "Usage: $0 [FILE-NAME]" >&2
    exit 2
    ;;
esac

rebuild-if-needed && ./mvnw -C package

# shellcheck disable=SC2086
exec java $class
