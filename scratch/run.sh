#!/usr/bin/env bash

package=x.scratch
jar=target/scratch-0-SNAPSHOT-jar-with-dependencies.jar

function mangle-classname() {
    local IFS=.

    local -a parts
    read -r -a parts <<<"$1"
    local last="${parts[-1]}"

    case "$last" in
    *-* | *Kt) ;;
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
0) set - -jar "$jar" ;;
*)
    class="$1"
    shift
    set - -cp "$jar" "$(mangle-classname "$package.$class")" "$@"
    ;;
esac

rebuild-if-needed && ./mvnw -C package

exec java "$@"
