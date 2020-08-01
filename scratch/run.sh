#!/usr/bin/env bash

jar=target/scratch-0-SNAPSHOT-jar-with-dependencies.jar

test -r $jar || ./mvnw -C package

function mangle-classname() {
    local name="$1"
    case $name in
    *-*) ;;
    *)
        echo "${name}Kt"
        return
        ;;
    esac

    name="${name//-/_}"
    name=""${name^}
    echo "${name}Kt"
}

case $# in
0) class="-jar $jar" ;;
1) class="-cp $jar x.scratch.$(mangle-classname ${1^})" ;;
*)
    echo "Usage: $0 [FILE-NAME]" >&2
    exit 2
    ;;
esac

exec java $class
